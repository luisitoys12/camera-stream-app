package tech.estacionkus.camerastream.ui.screens.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PlanInfo(
    val id: String,
    val name: String,
    val price: String,
    val description: String,
    val highlights: List<String>,
    val paymentUrl: String,
    val color: Color
)

@Composable
fun UpgradeScreen(
    lockedFeatureName: String? = null,
    requiredPlan: String? = null,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val plans = listOf(
        PlanInfo(
            id = "starter",
            name = "Starter",
            price = "\$9.99/mes",
            description = "Para streamers individuales",
            highlights = listOf(
                "SRT + 2 plataformas RTMP",
                "720p streaming",
                "Overlays de imagen y video nativo",
                "Adaptive bitrate",
                "Servidor SRT local + Cloudflared",
                "Chat multi-plataforma"
            ),
            paymentUrl = "https://buy.stripe.com/9B67sL9YIanMbK4eGcdAk00",
            color = Color(0xFF1565C0)
        ),
        PlanInfo(
            id = "pro",
            name = "Pro",
            price = "\$24.99/mes",
            description = "Para creadores serios",
            highlights = listOf(
                "Todo Starter +",
                "5 plataformas RTMP simultáneas",
                "1080p 60fps",
                "SRTLA bonding WiFi+datos",
                "Chroma key + múltiples escenas",
                "Panel web de control remoto",
                "Modo webcam para OBS/PC",
                "Grabación 1080p en background",
                "Export a Drive/OneDrive"
            ),
            paymentUrl = "https://buy.stripe.com/3cI8wPgn667w6pKfKgdAk01",
            color = Color(0xFF6A1B9A)
        ),
        PlanInfo(
            id = "agency",
            name = "Agency",
            price = "\$79.99/mes",
            description = "Para agencias y broadcasters",
            highlights = listOf(
                "Todo Pro +",
                "Plataformas RTMP ilimitadas",
                "Dashboard de analíticas",
                "Cuentas de equipo",
                "Programador de streams",
                "Branding personalizado",
                "API access programática",
                "SLA 99.9% + soporte dedicado"
            ),
            paymentUrl = "https://buy.stripe.com/00wbJ17QA0NcbK4eGcdAk02",
            color = Color(0xFFB71C1C)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A0A2E), Color(0xFF0D0D0D))))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                lockedFeatureName?.let {
                    Text("🔒 $it", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    requiredPlan?.let { plan ->
                        Text("Requiere $plan", color = Color.Gray, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Text("Elige tu plan", style = MaterialTheme.typography.headlineMedium,
                    color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("Sin prueba gratis. Sin sorpresas.",
                    color = Color.Gray, fontSize = 13.sp)
            }
        }

        // Plan cards
        plans.forEach { plan ->
            PlanCard(
                plan = plan,
                isHighlighted = plan.id == requiredPlan?.lowercase(),
                onSubscribe = { uriHandler.openUri(plan.paymentUrl) }
            )
        }

        // Coupon section
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.LocalOffer, null, tint = Color(0xFFFFD700))
                Spacer(Modifier.height(4.dp))
                Text("¿Eres creador de contenido?", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Solicita tu cupón exclusivo y obtén acceso Pro de por vida.",
                    color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PlanCard(plan: PlanInfo, isHighlighted: Boolean, onSubscribe: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) plan.color.copy(alpha = 0.15f) else Color(0xFF161616)
        ),
        border = if (isHighlighted) CardDefaults.outlinedCardBorder().let {
            androidx.compose.foundation.BorderStroke(2.dp, plan.color)
        } else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(plan.name, color = plan.color, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                if (isHighlighted) {
                    Spacer(Modifier.width(8.dp))
                    Badge(containerColor = plan.color) { Text("Recomendado", fontSize = 9.sp) }
                }
                Spacer(Modifier.weight(1f))
                Text(plan.price, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text(plan.description, color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            plan.highlights.forEach { highlight ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = plan.color, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(highlight, color = Color(0xFFCCCCCC), fontSize = 13.sp)
                }
                Spacer(Modifier.height(3.dp))
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = plan.color)
            ) {
                Text("Suscribirse — ${plan.price}", fontWeight = FontWeight.Bold)
            }
        }
    }
}
