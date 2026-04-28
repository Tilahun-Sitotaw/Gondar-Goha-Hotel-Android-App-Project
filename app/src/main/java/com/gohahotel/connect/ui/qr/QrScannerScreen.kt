package com.gohahotel.connect.ui.qr

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    onQrDecoded: (String) -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var scanned by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            when {
                cameraPermission.status.isGranted -> {
                    CameraPreview(
                        onQrScanned = { value ->
                            if (!scanned && value != lastResult) {
                                scanned   = true
                                lastResult = value
                                // Parse gohahotel:// deep links or route names
                                val route = when {
                                    value.startsWith("gohahotel://menu")    -> "menu"
                                    value.startsWith("gohahotel://rooms")   -> "rooms"
                                    value.startsWith("gohahotel://guide")   -> "cultural_guide"
                                    value.startsWith("gohahotel://concierge") -> "concierge"
                                    value.startsWith("gohahotel://room/")   -> "room_detail/${value.substringAfterLast("/")}"
                                    else -> value    // raw route string fallback
                                }
                                onQrDecoded(route)
                            }
                        }
                    )

                    // Scanning frame overlay
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .background(Color.Transparent)
                        ) {
                            // Corner indicators
                            listOf(
                                Alignment.TopStart, Alignment.TopEnd,
                                Alignment.BottomStart, Alignment.BottomEnd
                            ).forEach { alignment ->
                                Surface(
                                    modifier = Modifier.align(alignment).size(40.dp),
                                    color    = Color.Transparent
                                ) {
                                    Box(
                                        Modifier.fillMaxSize()
                                            .background(Color.Transparent)
                                    ) {
                                        // Corner decorators drawn via border approach
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Black.copy(0.6f)
                            ) {
                                Text(
                                    "Point camera at a Goha Hotel QR code",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color    = Color.White,
                                    style    = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                cameraPermission.status.shouldShowRationale -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null,
                            Modifier.size(72.dp), tint = GoldPrimary.copy(0.4f))
                        Spacer(Modifier.height(16.dp))
                        Text("Camera Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text("Camera access is needed to scan QR codes in your room or at the restaurant.",
                            color = Color.White.copy(0.7f),
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { cameraPermission.launchPermissionRequest() },
                            colors  = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape   = RoundedCornerShape(14.dp)
                        ) {
                            Text("Grant Permission", color = SurfaceDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Camera permission denied.\nPlease enable it in Settings.",
                            color = Color.White.copy(0.6f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(onQrScanned: (String) -> Unit) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView   = remember { PreviewView(context) }
    val scanner       = remember { BarcodeScanning.getClient() }

    AndroidView(
        factory  = { previewView },
        modifier = Modifier.fillMaxSize()
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                @androidx.camera.core.ExperimentalGetImage
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull { it.valueType == Barcode.TYPE_URL || it.rawValue != null }
                                ?.rawValue?.let { onQrScanned(it) }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                )
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(context))
    }
}
