package com.gohahotel.connect.ui.qr

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScanning
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

    // Request permission on entry
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            cameraPermission.status.isGranted -> {
                CameraPreview(
                    onQrScanned = { value ->
                        if (!scanned) {
                            scanned = true
                            // Parse gohahotel:// deep links or route names
                            val route = when {
                                value.startsWith("gohahotel://menu") -> "menu"
                                value.startsWith("gohahotel://rooms") -> "rooms"
                                value.startsWith("gohahotel://guide") -> "cultural_guide"
                                value.startsWith("gohahotel://concierge") -> "concierge"
                                value.startsWith("gohahotel://room/") -> "room_detail/${value.substringAfterLast("/")}"
                                else -> value // raw route string fallback
                            }
                            onQrDecoded(route)
                        }
                    }
                )
                
                // Modern Scanner UI Overlay
                ScannerOverlay(onBack = onBack)
            }
            cameraPermission.status.shouldShowRationale -> {
                PermissionRationaleUI(
                    onGrant = { cameraPermission.launchPermissionRequest() },
                    onBack = onBack
                )
            }
            else -> {
                // If denied or first time (but we launched it in Effect)
                if (!cameraPermission.status.isGranted) {
                    PermissionRationaleUI(
                        onGrant = { cameraPermission.launchPermissionRequest() },
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineY = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val scannerSize = 280.dp.toPx()
            val left = (canvasWidth - scannerSize) / 2
            val top = (canvasHeight - scannerSize) / 2
            val rect = Rect(left, top, left + scannerSize, top + scannerSize)
            val radius = 24.dp.toPx()

            // 1. Draw darkened background with a hole for the scanner
            with(drawContext.canvas.nativeCanvas) {
                val check = saveLayer(null, null)
                drawRect(Color.Black.copy(alpha = 0.7f))
                
                // Cut out the scanner area
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(radius),
                    blendMode = BlendMode.Clear
                )
                restoreToCount(check)
            }

            // 2. Draw styled corners
            val cornerLength = 40.dp.toPx()
            val strokeWidth = 5.dp.toPx()
            val color = GoldPrimary

            // Top Left Corner
            drawPath(
                path = Path().apply {
                    moveTo(rect.left, rect.top + cornerLength)
                    lineTo(rect.left, rect.top + radius)
                    arcTo(Rect(rect.left, rect.top, rect.left + radius * 2, rect.top + radius * 2), 180f, 90f, false)
                    lineTo(rect.left + cornerLength, rect.top)
                },
                color = color,
                style = Stroke(width = strokeWidth)
            )

            // Top Right Corner
            drawPath(
                path = Path().apply {
                    moveTo(rect.right - cornerLength, rect.top)
                    lineTo(rect.right - radius, rect.top)
                    arcTo(Rect(rect.right - radius * 2, rect.top, rect.right, rect.top + radius * 2), 270f, 90f, false)
                    lineTo(rect.right, rect.top + cornerLength)
                },
                color = color,
                style = Stroke(width = strokeWidth)
            )

            // Bottom Left Corner
            drawPath(
                path = Path().apply {
                    moveTo(rect.left, rect.bottom - cornerLength)
                    lineTo(rect.left, rect.bottom - radius)
                    arcTo(Rect(rect.left, rect.bottom - radius * 2, rect.left + radius * 2, rect.bottom), 90f, 90f, false)
                    lineTo(rect.left + cornerLength, rect.bottom)
                },
                color = color,
                style = Stroke(width = strokeWidth)
            )

            // Bottom Right Corner
            drawPath(
                path = Path().apply {
                    moveTo(rect.right - cornerLength, rect.bottom)
                    lineTo(rect.right - radius, rect.bottom)
                    arcTo(Rect(rect.right - radius * 2, rect.bottom - radius * 2, rect.right, rect.bottom), 0f, 90f, false)
                    lineTo(rect.right, rect.bottom - cornerLength)
                },
                color = color,
                style = Stroke(width = strokeWidth)
            )

            // 3. Draw the animated scanning line
            val lineY = rect.top + (rect.height * scanLineY.value)
            drawLine(
                color = GoldPrimary,
                start = Offset(rect.left + 16.dp.toPx(), lineY),
                end = Offset(rect.right - 16.dp.toPx(), lineY),
                strokeWidth = 3.dp.toPx(),
                alpha = 0.7f
            )
        }

        // Header UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.Black.copy(0.4f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "QR Scanner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Footer Instructions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(0.6f)
            ) {
                Text(
                    "Center the QR code in the frame",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Scan to access menus, rooms, and more",
                color = Color.White.copy(0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PermissionRationaleUI(onGrant: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(GoldPrimary.copy(0.1f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CameraAlt, null, Modifier.size(48.dp), tint = GoldPrimary)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Camera Access Needed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "To provide a seamless experience, we need your permission to use the camera for scanning QR codes throughout the hotel.",
            color = Color.White.copy(0.7f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onGrant,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Allow Camera", color = SurfaceDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
            Text("Not Now", color = Color.White.copy(0.6f))
        }
    }
}

@Composable
private fun CameraPreview(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
    } }
    val scanner = remember { BarcodeScanning.getClient() }

    AndroidView(
        factory = { previewView },
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
                            barcodes.firstOrNull { it.rawValue != null }?.rawValue?.let { 
                                onQrScanned(it)
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, 
                    CameraSelector.DEFAULT_BACK_CAMERA, 
                    preview, 
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
