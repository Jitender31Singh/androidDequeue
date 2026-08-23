package com.appvendor.feature_qr.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.print.PrintHelper
import com.appvendor.feature_qr.presentation.components.OrderLinkReadout
import com.appvendor.feature_qr.presentation.components.QrActionRow
import com.appvendor.feature_qr.presentation.components.QrCodeDisplayCard
import com.appvendor.feature_qr.presentation.components.QrCodeHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    viewModel: QrViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.savedFilePath) {
        state.savedFilePath?.let {
            Toast.makeText(context, "Saved QR Code to Photos", Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display QR Code", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 16.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.qrData != null) {
                val data = state.qrData!!
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QrCodeHeader(shopName = state.shopName ?: "Your Shop")

                    Spacer(modifier = Modifier.height(32.dp))

                    QrCodeDisplayCard(qrBitmap = data.bitmap)

                    Spacer(modifier = Modifier.height(48.dp))

                    QrActionRow(
                        onShare = { shareLink(context, data.url) },
                        onDownload = { viewModel.saveQrToFile() },
                        onCopy = { copyToClipboard(context, data.url) },
                        onPrint = { printBitmap(context, data.bitmap) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OrderLinkReadout(
                        url = data.url,
                        onCopy = { copyToClipboard(context, data.url) }
                    )
                }
            } else if (state.error == null) {
                Text(
                    text = "QR Code is not available yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Order Link", text)
    clipboardManager.setPrimaryClip(clip)
    Toast.makeText(context, "Link Copied", Toast.LENGTH_SHORT).show()
}

private fun shareLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Order now from our shop: $url")
    }
    context.startActivity(Intent.createChooser(intent, "Share Order Link"))
}

private fun printBitmap(context: Context, bitmap: Bitmap) {
    val printHelper = PrintHelper(context).apply {
        scaleMode = PrintHelper.SCALE_MODE_FIT
    }
    printHelper.printBitmap("Shop QR Code", bitmap)
}
