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
import androidx.compose.ui.graphics.graphicsLayer
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
    val flipV: Boolean = false
)

@HiltViewModel
class ImageEditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
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
            val bmp = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(path) }
            _originalBitmap.value = bmp
            _previewBitmap.value = bmp
            _isLoading.value = false
        }
    }

    fun updateParam(update: EditParams.() -> EditParams) {
        _editParams.value = _editParams.value.update()
        applyJob?.cancel()
        applyJob = viewModelScope.launch {
            delay(80)
            applyEdits()
        }
    }

    fun updateParamImmediate(update: EditParams.() -> EditParams) {
        _editParams.value = _editParams.value.update()
        applyEdits()
    }

    fun rotate(degrees: Float) { updateParamImmediate { copy(rotation = (rotation + degrees) % 360f) } }
    fun flipH() { updateParamImmediate { copy(flipH = !flipH) } }
    fun flipV() { updateParamImmediate { copy(flipV = !flipV) } }
    fun straighten(angle: Float) { updateParam { copy(straighten = angle) } }

    fun cropByRect(left: Float, top: Float, right: Float, bottom: Float, canvasW: Int, canvasH: Int) {
        val current = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val cropped = withContext(Dispatchers.IO) {
                val scaleX = current.width.toFloat() / canvasW
                val scaleY = current.height.toFloat() / canvasH
                val l = (left * scaleX).toInt().coerceIn(0, current.width - 1)
                val t = (top * scaleY).toInt().coerceIn(0, current.height - 1)
                val w = ((right - left) * scaleX).toInt().coerceIn(1, current.width - l)
                val h = ((bottom - top) * scaleY).toInt().coerceIn(1, current.height - t)
                Bitmap.createBitmap(current, l, t, w, h)
            }
            _originalBitmap.value = cropped
            _previewBitmap.value = cropped
            _isLoading.value = false
        }
    }

    fun cropByRatio(ratio: Float, canvasW: Int, canvasH: Int) {
        val current = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val cropped = withContext(Dispatchers.IO) {
                val bmpW = current.width.toFloat()
                val bmpH = current.height.toFloat()
                val currentRatio = bmpW / bmpH
                val left: Int; val top: Int; val width: Int; val height: Int
                if (currentRatio > ratio) {
                    height = current.height
                    width = (height * ratio).toInt()
                    left = ((bmpW - width) / 2).toInt()
                    top = 0
                } else {
                    width = current.width
                    height = (width / ratio).toInt()
                    left = 0
                    top = ((bmpH - height) / 2).toInt()
                }
                Bitmap.createBitmap(
                    current,
                    left.coerceAtLeast(0),
                    top.coerceAtLeast(0),
                    width.coerceAtMost(current.width - left.coerceAtLeast(0)),
                    height.coerceAtMost(current.height - top.coerceAtLeast(0))
                )
            }
            _originalBitmap.value = cropped
            _previewBitmap.value = cropped
            _isLoading.value = false
        }
    }

    private fun applyEdits() {
        val original = _originalBitmap.value ?: return
        val params = _editParams.value
        viewModelScope.launch {
            _previewBitmap.value = withContext(Dispatchers.IO) { applyFilters(original, params) }
        }
    }

    private fun applyFilters(src: Bitmap, p: EditParams): Bitmap {
        var bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        val matrix = Matrix()
        val totalRotation = p.rotation + p.straighten
        if (totalRotation != 0f) matrix.postRotate(totalRotation)
        if (p.flipH) matrix.preScale(-1f, 1f)
        if (p.flipV) matrix.preScale(1f, -1f)
        if (totalRotation != 0f || p.flipH || p.flipV)
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        val cm = ColorMatrix()
        cm.postConcat(ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, p.brightness * 255f,
            0f, 1f, 0f, 0f, p.brightness * 255f,
            0f, 0f, 1f, 0f, p.brightness * 255f,
            0f, 0f, 0f, 1f, 0f
        )))
        cm.postConcat(ColorMatrix(floatArrayOf(
            p.contrast, 0f, 0f, 0f, 128f * (1f - p.contrast),
            0f, p.contrast, 0f, 0f, 128f * (1f - p.contrast),
            0f, 0f, p.contrast, 0f, 128f * (1f - p.contrast),
            0f, 0f, 0f, 1f, 0f
        )))
        val satM = ColorMatrix()
        satM.setSaturation(p.saturation)
        cm.postConcat(satM)
        val warmth = p.temperature * 30f
        cm.postConcat(ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, warmth,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, -warmth,
            0f, 0f, 0f, 1f, 0f
        )))
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        val result = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        if (p.vignette > 0f) {
            val vp = Paint(Paint.ANTI_ALIAS_FLAG)
            vp.shader = RadialGradient(
                result.width / 2f, result.height / 2f,
                maxOf(result.width, result.height) / 1.5f,
                intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.argb((p.vignette * 180).toInt(), 0, 0, 0)),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), vp)
        }
        if (p.grain > 0f) {
            val gp = Paint().apply { alpha = (p.grain * 60).toInt() }
            val rnd = java.util.Random()
            for (x in 0 until result.width step 3) {
                for (y in 0 until result.height step 3) {
                    val n = rnd.nextInt(255)
                    gp.color = android.graphics.Color.rgb(n, n, n)
                    canvas.drawPoint(x.toFloat(), y.toFloat(), gp)
                }
            }
        }
        return result
    }

    fun save() {
        val current = _previewBitmap.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val path = withContext(Dispatchers.IO) {
                val file = File(File(originalPath).parentFile, "edited_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { current.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                file.absolutePath
            }
            _savedPath.value = path
            _isLoading.value = false
        }
    }

    fun resetEdits() {
        _editParams.value = EditParams()
        _previewBitmap.value = _originalBitmap.value
    }
}

enum class EditTab { CROP, ADJUST, EFFECTS }
enum class CropRatio(val label: String, val ratio: Float?) {
    FREE("Bebas", null),
    SQUARE("1:1", 1f),
    WIDE("16:9", 16f / 9f),
    STANDARD("4:3", 4f / 3f),
    PORTRAIT("3:4", 3f / 4f),
    CINEMA("21:9", 21f / 9f)
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
    var isCropMode by remember { mutableStateOf(false) }
    var selectedRatio by remember { mutableStateOf(CropRatio.FREE) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var cropStart by remember { mutableStateOf(Offset.Zero) }
    var cropEnd by remember { mutableStateOf(Offset.Zero) }
    var isCropDrawn by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    LaunchedEffect(savedPath) { if (savedPath != null) showSaved = true }
    LaunchedEffect(activeTab) { isCropMode = activeTab == EditTab.CROP; isCropDrawn = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Foto", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.resetEdits(); isCropDrawn = false }) {
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
                    if (isCropMode) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { canvasSize = it.size }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            cropStart = offset
                                            cropEnd = offset
                                            isCropDrawn = false
                                        },
                                        onDrag = { _, dragAmount ->
                                            cropEnd = Offset(
                                                (cropEnd.x + dragAmount.x).coerceIn(0f, canvasSize.width.toFloat()),
                                                (cropEnd.y + dragAmount.y).coerceIn(0f, canvasSize.height.toFloat())
                                            )
                                            isCropDrawn = true
                                        }
                                    )
                                }
                        ) {
                            drawImage(
                                bitmap!!.asImageBitmap(),
                                dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                            )
                            if (isCropDrawn) {
                                val left = minOf(cropStart.x, cropEnd.x)
                                val top = minOf(cropStart.y, cropEnd.y)
                                val right = maxOf(cropStart.x, cropEnd.x)
                                val bottom = maxOf(cropStart.y, cropEnd.y)
                                drawRect(color = Color.Black.copy(alpha = 0.5f), size = size)
                                drawRect(
                                    color = Color.Transparent,
                                    topLeft = Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                                    blendMode = BlendMode.Clear
                                )
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                                    style = Stroke(3.dp.toPx())
                                )
                                listOf(
                                    Offset(left, top), Offset(right, top),
                                    Offset(left, bottom), Offset(right, bottom)
                                ).forEach { drawCircle(Color.White, 8.dp.toPx(), it) }
                            }
                        }
                    } else {
                        AsyncImage(
                            model = bitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = params.straighten)
                        )
                    }
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF4CAF50)
                    )
                }
                if (showSaved) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) { Text("✓ Foto disimpan", color = Color.White, fontWeight = FontWeight.Bold) }
                    LaunchedEffect(Unit) { delay(2000); showSaved = false }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF111111)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EditTab.values().forEach { tab ->
                    TextButton(onClick = { activeTab = tab }) {
                        Text(
                            when (tab) {
                                EditTab.CROP -> "Crop & Putar"
                                EditTab.ADJUST -> "Adjust"
                                EditTab.EFFECTS -> "Efek"
                            },
                            color = if (activeTab == tab) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f),
                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)).padding(bottom = 16.dp)
            ) {
                when (activeTab) {
                    EditTab.CROP -> CropPanel(
                        params = params,
                        selectedRatio = selectedRatio,
                        isCropDrawn = isCropDrawn,
                        onRatioSelected = { ratio ->
                            selectedRatio = ratio
                            isCropDrawn = false
                            if (ratio.ratio != null && canvasSize != IntSize.Zero) {
                                viewModel.cropByRatio(ratio.ratio, canvasSize.width, canvasSize.height)
                            }
                        },
                        onCrop = {
                            if (isCropDrawn && canvasSize != IntSize.Zero) {
                                val left = minOf(cropStart.x, cropEnd.x)
                                val top = minOf(cropStart.y, cropEnd.y)
                                val right = maxOf(cropStart.x, cropEnd.x)
                                val bottom = maxOf(cropStart.y, cropEnd.y)
                                if (right - left > 10 && bottom - top > 10) {
                                    viewModel.cropByRect(left, top, right, bottom, canvasSize.width, canvasSize.height)
                                    isCropDrawn = false
                                }
                            }
                        },
                        onRotate = { viewModel.rotate(it) },
                        onFlipH = { viewModel.flipH() },
                        onFlipV = { viewModel.flipV() },
                        onStraighten = { viewModel.straighten(it) }
                    )
                    EditTab.ADJUST -> AdjustPanel(params = params, onUpdate = { viewModel.updateParam(it) })
                    EditTab.EFFECTS -> EffectsPanel(params = params, onUpdate = { viewModel.updateParam(it) })
                }
            }
        }
    }
}

@Composable
fun CropPanel(
    params: EditParams,
    selectedRatio: CropRatio,
    isCropDrawn: Boolean,
    onRatioSelected: (CropRatio) -> Unit,
    onCrop: () -> Unit,
    onRotate: (Float) -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onStraighten: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CropRatio.values().forEach { ratio ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedRatio == ratio) Color(0xFF4CAF50) else Color(0xFF2A2A2A))
                        .clickable { onRatioSelected(ratio) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        ratio.label,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (selectedRatio == ratio) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            EditorActionBtn(Icons.Default.RotateLeft, "Putar Kiri") { onRotate(-90f) }
            EditorActionBtn(Icons.Default.RotateRight, "Putar Kanan") { onRotate(90f) }
            EditorActionBtn(Icons.Default.Flip, "Balik H") { onFlipH() }
            EditorActionBtn(Icons.Default.Flip, "Balik V") { onFlipV() }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isCropDrawn) Color(0xFF4CAF50) else Color(0xFF2A2A2A), CircleShape)
                    .clickable { onCrop() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Crop, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            if (isCropDrawn) "Tap ikon Potong untuk memotong area" else "Seret jari pada gambar untuk memilih area",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("Luruskan: ${params.straighten.toInt()}°", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Slider(
                value = params.straighten,
                onValueChange = onStraighten,
                valueRange = -45f..45f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50))
            )
        }
    }
}

@Composable
fun AdjustPanel(params: EditParams, onUpdate: (EditParams.() -> EditParams) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AdjustSlider("Kecerahan", params.brightness, -1f..1f) { onUpdate { copy(brightness = it) } }
        AdjustSlider("Kontras", params.contrast - 1f, -1f..1f) { onUpdate { copy(contrast = 1f + it) } }
        AdjustSlider("Saturasi", params.saturation - 1f, -1f..1f) { onUpdate { copy(saturation = 1f + it) } }
        AdjustSlider("Highlight", params.highlights, -1f..1f) { onUpdate { copy(highlights = it) } }
        AdjustSlider("Shadow", params.shadows, -1f..1f) { onUpdate { copy(shadows = it) } }
        AdjustSlider("Suhu Warna", params.temperature, -1f..1f) { onUpdate { copy(temperature = it) } }
        AdjustSlider("Ketajaman", params.sharpness, 0f..1f) { onUpdate { copy(sharpness = it) } }
        AdjustSlider("Clarity", params.clarity, 0f..1f) { onUpdate { copy(clarity = it) } }
    }
}

@Composable
fun EffectsPanel(params: EditParams, onUpdate: (EditParams.() -> EditParams) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        HeavySlider("Vignette", params.vignette, 0f..1f) { onUpdate { copy(vignette = it) } }
        HeavySlider("Grain / Noise", params.grain, 0f..1f) { onUpdate { copy(grain = it) } }
    }
}

@Composable
fun AdjustSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text("%.2f".format(value), color = Color(0xFF4CAF50), fontSize = 12.sp)
        }
        Slider(
            value = value.coerceIn(range),
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50)),
            modifier = Modifier.height(32.dp)
        )
    }
}

@Composable
fun HeavySlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    var localValue by remember(value) { mutableStateOf(value) }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text("%.2f".format(localValue), color = Color(0xFF4CAF50), fontSize = 12.sp)
        }
        Slider(
            value = localValue.coerceIn(range),
            onValueChange = { localValue = it },
            onValueChangeFinished = { onChange(localValue) },
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50)),
            modifier = Modifier.height(32.dp)
        )
    }
}

@Composable
fun EditorActionBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp).background(Color(0xFF2A2A2A), CircleShape)
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
    }
}
