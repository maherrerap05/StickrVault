package com.example.myapplication.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onAddProduct: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraController = remember { ScannerCameraController() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val isCameraActive = uiState is ScannerUiState.CameraReady ||
        uiState is ScannerUiState.ProcessingCapture

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission && isCameraActive) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                controller = cameraController,
                onCaptureResult = { raw, code ->
                    viewModel.onCaptureResult(raw, code)
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!hasCameraPermission) "Permiso de cámara requerido"
                    else "Presiona 'Abrir cámara' para escanear",
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Escáner OCR",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                if (isCameraActive) {
                    Text(
                        text = "Alinea el código en el recuadro y pulsa Capturar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (isCameraActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(width = 140.dp, height = 72.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (val s = uiState) {
                    is ScannerUiState.ProcessingCapture -> {
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Text("Leyendo código...", color = Color.White)
                        }
                    }

                    is ScannerUiState.ProductFound -> ResultCard(
                        icon = {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        },
                        title = s.product.name,
                        subtitle = "Código: ${s.code} · Stock: ${s.product.currentStock}",
                        onDismiss = { viewModel.reset() }
                    )

                    is ScannerUiState.ProductNotFound -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ResultCard(
                            icon = {
                                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                            },
                            title = "Producto no encontrado",
                            subtitle = "Código: ${s.code}",
                            onDismiss = { viewModel.reset() }
                        )
                        Button(
                            onClick = {
                                onAddProduct(s.code)
                                viewModel.reset()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar producto")
                        }
                        OutlinedButton(
                            onClick = { viewModel.backToCamera() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Capturar otro")
                        }
                    }

                    is ScannerUiState.CodeNotRecognized -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ResultCard(
                            icon = {
                                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                            },
                            title = "No se detectó el código",
                            subtitle = "Intenta de nuevo con mejor luz y el código dentro del recuadro",
                            onDismiss = { viewModel.backToCamera() }
                        )
                        OutlinedButton(
                            onClick = { viewModel.backToCamera() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Capturar otro")
                        }
                    }

                    is ScannerUiState.Searching -> {
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Text("Buscando: ${s.code}", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    is ScannerUiState.Error -> Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )

                    else -> {}
                }

                when {
                    isCameraActive -> {
                        Button(
                            onClick = {
                                viewModel.onCaptureRequested()
                                cameraController.requestCapture()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is ScannerUiState.ProcessingCapture
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Capturar código")
                        }
                        OutlinedButton(
                            onClick = { viewModel.closeCamera() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cerrar cámara")
                        }
                    }

                    uiState !is ScannerUiState.Searching &&
                        uiState !is ScannerUiState.ProductFound &&
                        uiState !is ScannerUiState.ProductNotFound &&
                        uiState !is ScannerUiState.CodeNotRecognized -> {
                        Button(
                            onClick = { viewModel.openCamera() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = hasCameraPermission
                        ) {
                            Text("Abrir cámara")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    controller: ScannerCameraController,
    onCaptureResult: (rawText: String, extractedCode: String?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val mainExecutor = ContextCompat.getMainExecutor(ctx)

            ProcessCameraProvider.getInstance(ctx).also { future ->
                future.addListener({
                    val cameraProvider = future.get()

                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().apply {
                            setAnalyzer(executor) { imageProxy ->
                                if (!controller.consumeCaptureRequest()) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    recognizer.process(image)
                                        .addOnSuccessListener { result ->
                                            val raw = result.text.trim()
                                            val code = if (raw.isNotBlank()) {
                                                OcrCodeExtractor.extractFromMlKit(
                                                    text = result,
                                                    imageWidth = imageProxy.width,
                                                    imageHeight = imageProxy.height
                                                )
                                            } else {
                                                null
                                            }
                                            mainExecutor.execute {
                                                onCaptureResult(raw, code)
                                            }
                                        }
                                        .addOnFailureListener {
                                            mainExecutor.execute {
                                                onCaptureResult("", null)
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    onCaptureResult("", null)
                                    imageProxy.close()
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
            previewView
        }
    )
}
