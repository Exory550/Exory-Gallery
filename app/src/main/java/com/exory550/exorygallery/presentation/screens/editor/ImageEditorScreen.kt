package com.exory550.exorygallery.presentation.screens.editor

import android.content.Context
import android.graphics.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import javax.inject.Inject

data class EditParams(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val temperature: Float = 0f,
    val sharpness: Float = 0f,
    val clarity: Float = 0f,
    val vignette: Float = 0f,
    val grain: Float = 0f,
    val rotation: Float = 0f,
    val straighten: Float = 0f,
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f
)

@HiltViewModel
class ImageEditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var sourceBitmap: Bitmap? = null
    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap
    private val _editParams = MutableStateFlow(EditParams())
    val editParams: StateFlow<EditParams> = _editParams
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _savedPath = MutableStateFlow<String?>(null)
    val savedPath: StateFlow<String?> = _savedPath
    private var originalPath = ""
    private var applyJob: Job? = null

    fun loadImage(path: String) {
        originalPath = path
        viewModelScope.launch {
            _isLoading.value = true
            sourceBitmap = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(path) }
            _previewBitmap.value = sourceBitmap
            _editParams.value = EditParams()
            _isLoading.value = false
        }
    }

    fun updateParam(update: EditParams.() -> EditParams) {
        _editParams.value = _editParams.value.update()
        scheduleApply()
    }

    fun updateParamNow(update: EditParams.() -> EditParams) {
        _editParams.value = _editParams.value.update()
        applyNow()
    }

    private fun scheduleApply() {
        applyJob?.cancel()
        applyJob = viewModelScope.launch {
            delay(150)
            applyNow()
        }
    }

    private fun applyNow() {
        val src = sourceBitmap ?: return
        val p = _editParams.value
        viewModelScope.launch(Dispatchers.Default) {
            _previewBitmap.value = renderBitmap(src, p)
        }
    }

    fun rotate(degrees: Float) { updateParamNow { copy(rotation = (rotation + degrees) % 360f) } }
    fun flipH() { updateParamNow { copy(flipH = !flipH) } }
    fun flipV() { updateParamNow { copy(flipV = !flipV) } }
    fun straighten(angle: Float) { updateParam { copy(straighten = angle) } }

    fun setCropRatio(ratio: Float) {
        val src = sourceBitmap ?: return
        val bmpW = src.width.toFloat()
        val bmpH = src.height.toFloat()
        val currentRatio = bmpW / bmpH
        val left: Float; val top: Float; val right: Float; val bottom: Float
        if (currentRatio > ratio) {
            val newW = bmpH * ratio
            left = ((bmpW - newW) / 2f) / bmpW
            top = 0f
            right = left + newW / bmpW
            bottom = 1f
        } else {
            val newH = bmpW / ratio
            left = 0f
            top = ((bmpH - newH) / 2f) / bmpH
            right = 1f
            bottom = top + newH / bmpH
        }
        updateParamNow { copy(cropLeft = left, cropTop = top, cropRight = right, cropBottom = bottom) }
    }

    fun resetCrop() {
        updateParamNow { copy(cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f) }
    }

    fun setCropRect(left: Float, top: Float, right: Float, bottom: Float, canvasW: Int, canvasH: Int) {
        val src = sourceBitmap ?: return
        val cl = (left / canvasW).coerceIn(0f, 1f)
        val ct = (top / canvasH).coerceIn(0f, 1f)
        val cr = (right / canvasW).coerceIn(0f, 1f)
        val cb = (bottom / canvasH).coerceIn(0f, 1f)
        updateParamNow { copy(cropLeft = cl, cropTop = ct, cropRight = cr, cropBottom = cb) }
    }

    private fun renderBitmap(src: Bitmap, p: EditParams): Bitmap {
        var bmp = src.copy(Bitmap.Config.ARGB_8888, true)

        val matrix = Matrix()
        val totalRot = p.rotation + p.straighten
        if (totalRot != 0f) matrix.postRotate(totalRot)
        if (p.flipH) matrix.preScale(-1f, 1f)
        if (p.flipV) matrix.preScale(1f, -1f)
        if (totalRot != 0f || p.flipH || p.flipV)
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)

        val hasCrop = p.cropLeft > 0f || p.cropTop > 0f || p.cropRight < 1f || p.cropBottom < 1f
        if (hasCrop) {
            val l = (p.cropLeft * bmp.width).toInt().coerceIn(0, bmp.width - 1)
            val t = (p.cropTop * bmp.height).toInt().coerceIn(0, bmp.height - 1)
            val r = (p.cropRight * bmp.width).toInt().coerceIn(l + 1, bmp.width)
            val b = (p.cropBottom * bmp.height).toInt().coerceIn(t + 1, bmp.height)
            bmp = Bitmap.createBitmap(bmp, l, t, r - l, b - t)
        }

        val cm = ColorMatrix()
        cm.postConcat(ColorMatrix(floatArrayOf(
            1f,0f,0f,0f,p.brightness*255f, 0f,1f,0f,0f,p.brightness*255f,
            0f,0f,1f,0f,p.brightness*255f, 0f,0f,0f,1f,0f)))
        cm.postConcat(ColorMatrix(floatArrayOf(
            p.contrast,0f,0f,0f,128f*(1f-p.contrast), 0f,p.contrast,0f,0f,128f*(1f-p.contrast),
            0f,0f,p.contrast,0f,128f*(1f-p.contrast), 0f,0f,0f,1f,0f)))
        val satM = ColorMatrix(); satM.setSaturation(p.saturation); cm.postConcat(satM)
        val w = p.temperature * 30f
        cm.postConcat(ColorMatrix(floatArrayOf(1f,0f,0f,0f,w, 0f,1f,0f,0f,0f, 0f,0f,1f,0f,-w, 0f,0f,0f,1f,0f)))

        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        val result = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(bmp, 0f, 0f, paint)

        if (p.vignette > 0f) {
            val vp = Paint(Paint.ANTI_ALIAS_FLAG)
            vp.shader = RadialGradient(
                result.width/2f, result.height/2f,
                maxOf(result.width, result.height) * 0.8f,
                intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.argb((p.vignette*220).toInt().coerceIn(0,255),0,0,0)),
                floatArrayOf(0.3f, 1f), Shader.TileMode.CLAMP)
            canvas.drawRect(0f,0f,result.width.toFloat(),result.height.toFloat(),vp)
        }

        if (p.grain > 0f) {
            val gb = Bitmap.createBitmap(result.width, result.height, Bitmap.Config.ARGB_8888)
            val gc = Canvas(gb)
            val gp = Paint()
            val rnd = java.util.Random(System.currentTimeMillis())
            val intensity = (p.grain * 100).toInt()
            for (x in 0 until result.width step 2) for (y in 0 until result.height step 2) {
                val n = rnd.nextInt(255)
                gp.color = android.graphics.Color.argb(rnd.nextInt(intensity.coerceAtLeast(1)), n, n, n)
                gc.drawPoint(x.toFloat(), y.toFloat(), gp)
            }
            canvas.drawBitmap(gb, 0f, 0f, Paint().apply { alpha = (p.grain*160).toInt().coerceIn(0,160) })
            gb.recycle()
        }

        return result
    }

    fun save() {
        val src = sourceBitmap ?: return
        val p = _editParams.value
        viewModelScope.launch {
            _isLoading.value = true
            val path = withContext(Dispatchers.IO) {
                val bmp = renderBitmap(src, p)
                val file = File(File(originalPath).parentFile, "edited_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                file.absolutePath
            }
            _savedPath.value = path
            _isLoading.value = false
        }
    }

    fun resetEdits() {
        _editParams.value = EditParams()
        _previewBitmap.value = sourceBitmap
    }
}

enum class EditTab { CROP, ADJUST, EFFECTS }
enum class CropRatio(val label: String, val ratio: Float?) {
    FREE("Bebas", null), SQUARE("1:1", 1f), WIDE("16:9", 16f/9f),
    STANDARD("4:3", 4f/3f), PORTRAIT("3:4", 3f/4f), CINEMA("21:9", 21f/9f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    navController: NavController,
    encodedPath: String,
    viewModel: ImageEditorViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val path = URLDecoder.decode(encodedPath, "UTF-8")
    LaunchedEffect(path) { viewModel.loadImage(path) }

    val bitmap by viewModel.previewBitmap.collectAsState()
    val params by viewModel.editParams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val savedPath by viewModel.savedPath.collectAsState()

    var activeTab by remember { mutableStateOf(EditTab.ADJUST) }
    var selectedRatio by remember { mutableStateOf(CropRatio.FREE) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var cropStart by remember { mutableStateOf(Offset.Zero) }
    var cropEnd by remember { mutableStateOf(Offset.Zero) }
    var isCropDrawn by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    LaunchedEffect(savedPath) { if (savedPath != null) showSaved = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Foto", color = Color.White) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = {
                    TextButton(onClick = { viewModel.resetEdits(); selectedRatio = CropRatio.FREE; isCropDrawn = false }) {
                        Text("Reset", color = Color.White.copy(alpha = 0.7f))
                    }
                    TextButton(onClick = { viewModel.save() }) {
                        Text("Simpan", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    if (activeTab == EditTab.CROP) {
                        CropOverlay(
                            bitmap = bitmap!!,
                            canvasSize = canvasSize,
                            cropStart = cropStart,
                            cropEnd = cropEnd,
                            isCropDrawn = isCropDrawn,
                            onSizeChanged = { canvasSize = it },
                            onCropChange = { s, e, drawn -> cropStart = s; cropEnd = e; isCropDrawn = drawn }
                        )
                    } else {
                        AsyncImage(model = bitmap, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                    }
                }
                if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4CAF50))
                if (showSaved) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).background(Color(0xFF4CAF50), RoundedCornerShape(8.dp)).padding(horizontal=20.dp, vertical=10.dp)) {
                        Text("✓ Foto disimpan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    LaunchedEffect(Unit) { delay(2000); showSaved = false }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF111111)), horizontalArrangement = Arrangement.SpaceEvenly) {
                EditTab.values().forEach { tab ->
                    TextButton(onClick = { activeTab = tab; isCropDrawn = false }) {
                        Text(when(tab){ EditTab.CROP->"Crop & Putar"; EditTab.ADJUST->"Adjust"; EditTab.EFFECTS->"Efek" },
                            color = if(activeTab==tab) Color(0xFF4CAF50) else Color.White.copy(alpha=0.6f),
                            fontWeight = if(activeTab==tab) FontWeight.Bold else FontWeight.Normal, fontSize=13.sp)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)).padding(bottom=16.dp)) {
                when (activeTab) {
                    EditTab.CROP -> CropPanel(
                        params = params,
                        selectedRatio = selectedRatio,
                        isCropDrawn = isCropDrawn,
                        onRatioSelected = { ratio ->
                            selectedRatio = ratio
                            isCropDrawn = false
                            if (ratio.ratio != null) viewModel.setCropRatio(ratio.ratio)
                            else viewModel.resetCrop()
                        },
                        onCrop = {
                            if (isCropDrawn && canvasSize != IntSize.Zero) {
                                val l = minOf(cropStart.x, cropEnd.x)
                                val t = minOf(cropStart.y, cropEnd.y)
                                val r = maxOf(cropStart.x, cropEnd.x)
                                val b = maxOf(cropStart.y, cropEnd.y)
                                if (r-l > 20 && b-t > 20) {
                                    viewModel.setCropRect(l, t, r, b, canvasSize.width, canvasSize.height)
                                    isCropDrawn = false
                                    selectedRatio = CropRatio.FREE
                                }
                            }
                        },
                        onRotate = { viewModel.rotate(it) },
                        onFlipH = { viewModel.flipH() },
                        onFlipV = { viewModel.flipV() },
                        onStraighten = { viewModel.straighten(it) }
                    )
                    EditTab.ADJUST -> AdjustPanel(params=params, onUpdate={ viewModel.updateParam(it) })
                    EditTab.EFFECTS -> EffectsPanel(params=params, onUpdate={ viewModel.updateParam(it) })
                }
            }
        }
    }
}

@Composable
fun CropOverlay(
    bitmap: android.graphics.Bitmap,
    canvasSize: IntSize,
    cropStart: Offset,
    cropEnd: Offset,
    isCropDrawn: Boolean,
    onSizeChanged: (IntSize) -> Unit,
    onCropChange: (Offset, Offset, Boolean) -> Unit
) {
    var left by remember { mutableStateOf(0f) }
    var top by remember { mutableStateOf(0f) }
    var right by remember { mutableStateOf(0f) }
    var bottom by remember { mutableStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }
    var activeHandle by remember { mutableStateOf(-1) }
    val handleSize = 50f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                onSizeChanged(coords.size)
                if (!initialized) {
                    val pad = 60f
                    left = pad
                    top = pad
                    right = coords.size.width.toFloat() - pad
                    bottom = coords.size.height.toFloat() - pad
                    initialized = true
                    onCropChange(Offset(left, top), Offset(right, bottom), true)
                }
            }
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectDragGestures(
                    onDragStart = { pos ->
                        val cx = pos.x
                        val cy = pos.y
                        val midX = (left + right) / 2f
                        val midY = (top + bottom) / 2f
                        activeHandle = when {
                            kotlin.math.abs(cx - left) < handleSize && kotlin.math.abs(cy - top) < handleSize -> 0
                            kotlin.math.abs(cx - right) < handleSize && kotlin.math.abs(cy - top) < handleSize -> 1
                            kotlin.math.abs(cx - left) < handleSize && kotlin.math.abs(cy - bottom) < handleSize -> 2
                            kotlin.math.abs(cx - right) < handleSize && kotlin.math.abs(cy - bottom) < handleSize -> 3
                            kotlin.math.abs(cx - midX) < handleSize && kotlin.math.abs(cy - top) < handleSize -> 4
                            kotlin.math.abs(cx - midX) < handleSize && kotlin.math.abs(cy - bottom) < handleSize -> 5
                            kotlin.math.abs(cx - left) < handleSize && kotlin.math.abs(cy - midY) < handleSize -> 6
                            kotlin.math.abs(cx - right) < handleSize && kotlin.math.abs(cy - midY) < handleSize -> 7
                            else -> -1
                        }
                    },
                    onDrag = { _, dragAmount ->
                        val dx = dragAmount.x
                        val dy = dragAmount.y
                        val maxW = canvasSize.width.toFloat()
                        val maxH = canvasSize.height.toFloat()
                        val minSize = 80f
                        when (activeHandle) {
                            0 -> { left = (left + dx).coerceIn(0f, right - minSize); top = (top + dy).coerceIn(0f, bottom - minSize) }
                            1 -> { right = (right + dx).coerceIn(left + minSize, maxW); top = (top + dy).coerceIn(0f, bottom - minSize) }
                            2 -> { left = (left + dx).coerceIn(0f, right - minSize); bottom = (bottom + dy).coerceIn(top + minSize, maxH) }
                            3 -> { right = (right + dx).coerceIn(left + minSize, maxW); bottom = (bottom + dy).coerceIn(top + minSize, maxH) }
                            4 -> { top = (top + dy).coerceIn(0f, bottom - minSize) }
                            5 -> { bottom = (bottom + dy).coerceIn(top + minSize, maxH) }
                            6 -> { left = (left + dx).coerceIn(0f, right - minSize) }
                            7 -> { right = (right + dx).coerceIn(left + minSize, maxW) }
                        }
                        onCropChange(Offset(left, top), Offset(right, bottom), true)
                    }
                )
            }
    ) {
        drawImage(
            bitmap.asImageBitmap(),
            dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
        )

        val dimColor = Color.Black.copy(alpha = 0.55f)
        drawRect(dimColor, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(left, size.height))
        drawRect(dimColor, topLeft = Offset(right, 0f), size = androidx.compose.ui.geometry.Size(size.width - right, size.height))
        drawRect(dimColor, topLeft = Offset(left, 0f), size = androidx.compose.ui.geometry.Size(right - left, top))
        drawRect(dimColor, topLeft = Offset(left, bottom), size = androidx.compose.ui.geometry.Size(right - left, size.height - bottom))

        val cropW = right - left
        val cropH = bottom - top
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(cropW, cropH),
            style = Stroke(2.dp.toPx())
        )

        for (i in 1..2) {
            drawLine(Color.White.copy(alpha = 0.4f), Offset(left + cropW * i / 3f, top), Offset(left + cropW * i / 3f, bottom), 1.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.4f), Offset(left, top + cropH * i / 3f), Offset(right, top + cropH * i / 3f), 1.dp.toPx())
        }

        val hs = 16.dp.toPx()
        val sw = 4.dp.toPx()
        drawLine(Color.White, Offset(left, top), Offset(left + hs, top), sw)
        drawLine(Color.White, Offset(left, top), Offset(left, top + hs), sw)
        drawLine(Color.White, Offset(right, top), Offset(right - hs, top), sw)
        drawLine(Color.White, Offset(right, top), Offset(right, top + hs), sw)
        drawLine(Color.White, Offset(left, bottom), Offset(left + hs, bottom), sw)
        drawLine(Color.White, Offset(left, bottom), Offset(left, bottom - hs), sw)
        drawLine(Color.White, Offset(right, bottom), Offset(right - hs, bottom), sw)
        drawLine(Color.White, Offset(right, bottom), Offset(right, bottom - hs), sw)

        val midX = (left + right) / 2f
        val midY = (top + bottom) / 2f
        drawLine(Color.White, Offset(midX - hs/2, top), Offset(midX + hs/2, top), sw)
        drawLine(Color.White, Offset(midX - hs/2, bottom), Offset(midX + hs/2, bottom), sw)
        drawLine(Color.White, Offset(left, midY - hs/2), Offset(left, midY + hs/2), sw)
        drawLine(Color.White, Offset(right, midY - hs/2), Offset(right, midY + hs/2), sw)
    }
}


@Composable
fun CropPanel(
    params: EditParams, selectedRatio: CropRatio, isCropDrawn: Boolean,
    onRatioSelected: (CropRatio)->Unit, onCrop: ()->Unit,
    onRotate: (Float)->Unit, onFlipH: ()->Unit, onFlipV: ()->Unit, onStraighten: (Float)->Unit
) {
    Column(modifier=Modifier.padding(8.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(modifier=Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            CropRatio.values().forEach { ratio ->
                Box(modifier=Modifier.clip(RoundedCornerShape(6.dp))
                    .background(if(selectedRatio==ratio) Color(0xFF4CAF50) else Color(0xFF2A2A2A))
                    .clickable { onRatioSelected(ratio) }
                    .padding(horizontal=14.dp, vertical=8.dp)) {
                    Text(ratio.label, color=Color.White, fontSize=13.sp, fontWeight=if(selectedRatio==ratio) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly) {
            EditorActionBtn(Icons.Default.RotateLeft,"Putar Kiri"){onRotate(-90f)}
            EditorActionBtn(Icons.Default.RotateRight,"Putar Kanan"){onRotate(90f)}
            EditorActionBtn(Icons.Default.Flip,"Balik H"){onFlipH()}
            EditorActionBtn(Icons.Default.Flip,"Balik V"){onFlipV()}
            Box(modifier=Modifier.size(44.dp).background(if(isCropDrawn) Color(0xFF4CAF50) else Color(0xFF2A2A2A), CircleShape).clickable{onCrop()}, contentAlignment=Alignment.Center) {
                Icon(Icons.Default.Crop, null, tint=Color.White, modifier=Modifier.size(20.dp))
            }
        }
        Text(if(isCropDrawn) "✓ Tap ikon Potong untuk memotong area" else "Seret jari pada gambar untuk pilih area",
            color=if(isCropDrawn) Color(0xFF4CAF50) else Color.White.copy(alpha=0.5f), fontSize=11.sp, modifier=Modifier.align(Alignment.CenterHorizontally))
        Column(modifier=Modifier.padding(horizontal=8.dp)) {
            Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("Luruskan", color=Color.White.copy(alpha=0.8f), fontSize=12.sp)
                Text("${params.straighten.toInt()}°", color=Color(0xFF4CAF50), fontSize=12.sp)
            }
            Slider(value=params.straighten, onValueChange=onStraighten, valueRange=-45f..45f,
                colors=SliderDefaults.colors(thumbColor=Color(0xFF4CAF50), activeTrackColor=Color(0xFF4CAF50)))
        }
    }
}

@Composable
fun AdjustPanel(params: EditParams, onUpdate: (EditParams.()->EditParams)->Unit) {
    Column(modifier=Modifier.padding(horizontal=16.dp), verticalArrangement=Arrangement.spacedBy(2.dp)) {
        AdjustSlider("Kecerahan", params.brightness, -1f..1f){ onUpdate{copy(brightness=it)} }
        AdjustSlider("Kontras", params.contrast-1f, -1f..1f){ onUpdate{copy(contrast=1f+it)} }
        AdjustSlider("Saturasi", params.saturation-1f, -1f..1f){ onUpdate{copy(saturation=1f+it)} }
        AdjustSlider("Highlight", params.highlights, -1f..1f){ onUpdate{copy(highlights=it)} }
        AdjustSlider("Shadow", params.shadows, -1f..1f){ onUpdate{copy(shadows=it)} }
        AdjustSlider("Suhu Warna", params.temperature, -1f..1f){ onUpdate{copy(temperature=it)} }
        AdjustSlider("Ketajaman", params.sharpness, 0f..1f){ onUpdate{copy(sharpness=it)} }
        AdjustSlider("Clarity", params.clarity, 0f..1f){ onUpdate{copy(clarity=it)} }
    }
}

@Composable
fun EffectsPanel(params: EditParams, onUpdate: (EditParams.()->EditParams)->Unit) {
    Column(modifier=Modifier.padding(horizontal=16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
        HeavySlider("Vignette", params.vignette, 0f..1f){ onUpdate{copy(vignette=it)} }
        HeavySlider("Grain / Noise", params.grain, 0f..1f){ onUpdate{copy(grain=it)} }
    }
}

@Composable
fun AdjustSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float)->Unit) {
    var local by remember(value) { mutableStateOf(value) }
    Column {
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
            Text(label, color=Color.White.copy(alpha=0.8f), fontSize=12.sp)
            Text("%.2f".format(local), color=Color(0xFF4CAF50), fontSize=12.sp)
        }
        Slider(value=local.coerceIn(range), onValueChange={ local=it; onChange(it) }, valueRange=range,
            colors=SliderDefaults.colors(thumbColor=Color(0xFF4CAF50), activeTrackColor=Color(0xFF4CAF50)), modifier=Modifier.height(32.dp))
    }
}

@Composable
fun HeavySlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float)->Unit) {
    var local by remember(value) { mutableStateOf(value) }
    Column {
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
            Text(label, color=Color.White.copy(alpha=0.8f), fontSize=12.sp)
            Text("%.2f".format(local), color=Color(0xFF4CAF50), fontSize=12.sp)
        }
        Slider(value=local.coerceIn(range), onValueChange={local=it}, onValueChangeFinished={onChange(local)},
            valueRange=range, colors=SliderDefaults.colors(thumbColor=Color(0xFF4CAF50), activeTrackColor=Color(0xFF4CAF50)), modifier=Modifier.height(32.dp))
    }
}

@Composable
fun EditorActionBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: ()->Unit) {
    Column(horizontalAlignment=Alignment.CenterHorizontally) {
        IconButton(onClick=onClick, modifier=Modifier.size(44.dp).background(Color(0xFF2A2A2A), CircleShape)) {
            Icon(icon, null, tint=Color.White, modifier=Modifier.size(20.dp))
        }
        Text(label, color=Color.White.copy(alpha=0.6f), fontSize=9.sp)
    }
}
