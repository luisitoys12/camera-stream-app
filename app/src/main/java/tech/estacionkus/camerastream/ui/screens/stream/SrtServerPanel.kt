package tech.estacionkus.camerastream.ui.screens.stream

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun SrtServerPanel(
    isRunning: Boolean,
    localUrl: String,
    tunnelUrl: String?,
    connectedClients: Int,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A2E), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Router, contentDescription = null,
                tint = if (isRunning) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Servidor SRT Local", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.weight(1f))
            if (isRunning && connectedClients > 0) {
                Badge(containerColor = Color(0xFF4CAF50)) {
                    Text("$connectedClients cliente(s)", fontSize = 10.sp)
                }
            }
        }

        if (isRunning) {
            // Local URL
            InfoRow(label = "Local", value = localUrl, icon = Icons.Default.Lan)

            // Tunnel URL
            tunnelUrl?.let { url ->
                InfoRow(label = "Internet", value = url, icon = Icons.Default.Cloud)

                // QR Code
                val qrBitmap = remember(url) { generateQr(url) }
                qrBitmap?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR SRT URL",
                            modifier = Modifier.size(140.dp)
                        )
                    }
                    Text(
                        "Escanea para conectar OBS/encoder externo",
                        fontSize = 11.sp, color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } ?: Text(
                "⏳ Generando túnel Cloudflared...",
                color = Color.Yellow, fontSize = 12.sp
            )
        }

        Button(
            onClick = if (isRunning) onStop else onStart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color(0xFFB71C1C) else Color(0xFF1565C0)
            )
        ) {
            Icon(
                if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(if (isRunning) "Detener servidor" else "Iniciar servidor SRT")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D1A), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

private fun generateQr(url: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val matrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300)
        val bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565)
        for (x in 0 until 300) for (y in 0 until 300)
            bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        bmp
    } catch (e: Exception) { null }
}
