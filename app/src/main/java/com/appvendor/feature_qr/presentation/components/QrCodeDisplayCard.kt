package com.appvendor.feature_qr.presentation.components

import android.app.Activity
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh

@Composable
fun QrCodeDisplayCard(
    qrBitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isBrightnessBoosted by remember { mutableStateOf(false) }

    // Brightness boost logic
    DisposableEffect(isBrightnessBoosted) {
        val window = (context as? Activity)?.window
        val layoutParams = window?.attributes
        val originalBrightness = layoutParams?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

        if (isBrightnessBoosted && window != null) {
            layoutParams?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            window.attributes = layoutParams
        }

        onDispose {
            if (isBrightnessBoosted && window != null) {
                layoutParams?.screenBrightness = originalBrightness
                window.attributes = layoutParams
            }
        }
    }

    // Intro animation
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(qrBitmap) {
        if (qrBitmap != null) {
            scale.animateTo(1f, animationSpec = tween(400))
            alpha.animateTo(1f, animationSpec = tween(400))
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(0.85f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .scale(scale.value)
                            .clip(RoundedCornerShape(16.dp))
                            // Subtle viewfinder frame
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize(),
                            alpha = alpha.value
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { isBrightnessBoosted = !isBrightnessBoosted },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isBrightnessBoosted) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessHigh,
                            contentDescription = "Brightness",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBrightnessBoosted) "Brightness Boosted" else "Boost Brightness")
                    }
                }
            } else {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
