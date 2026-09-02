package com.showerly.app.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.showerly.app.di.AppContainer
import com.showerly.app.domain.model.BathroomStatus
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val EmptyColor = Color(0xFF2E8BFF)
private val BusyColor = Color(0xFFFFC107)
private val FullColor = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(container: AppContainer) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val state by vm.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Showerly") },
                actions = {
                    IconButton(onClick = vm::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                state.error != null && state.bathrooms.isEmpty() -> ErrorPanel(state.error!!, vm::refresh)
                state.bathrooms.isEmpty() && state.isLoading -> LoadingPanel()
                state.bathrooms.isEmpty() -> ErrorPanel(state.error ?: "当前筛选下暂无浴室", vm::refresh)
                else -> CrowdPager(
                    state = state,
                    onOpenBays = { scope.launch { snackbar.showSnackbar("浴位示意图尚未接入，敬请期待") } }
                )
            }
        }
    }
}

@Composable
private fun CrowdPager(state: HomeUiState, onOpenBays: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { state.bathrooms.size })
    LaunchedEffect(state.bathrooms.size) {
        if (pagerState.currentPage >= state.bathrooms.size && pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }
    Column(Modifier.fillMaxSize()) {
        OrderChip(state)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.weight(1f)
        ) { page ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                BathroomCard(state.bathrooms[page], state.timeText, onOpenBays)
            }
        }
        BottomHint(state)
    }
}

@Composable
private fun OrderChip(state: HomeUiState) {
    Text(
        text = "${state.gender.label}浴 · ${state.campus.label} · ${state.bathrooms.size} 个浴室",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

@Composable
private fun BottomHint(state: HomeUiState) {
    Text(
        text = "左右滑动切换浴室",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    )
}

@Composable
private fun BathroomCard(bathroom: BathroomStatus, timeText: String, onOpenBays: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(max = 400.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bathroom.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(onClick = {}, label = { Text(bathroom.statusLabel) })
            }
            Spacer(Modifier.height(6.dp))
            BreathingBall(bathroom, onOpenBays)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${bathroom.useCount} 人在洗",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "空位 ${bathroom.vacant} / 容量 ${bathroom.capacity}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { bathroom.occupancyRatio },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = ratioColor(bathroom.occupancyRatio),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            PlaceholderRow("历史人数趋势", "需后端 D1 采集历史后展示")
            Spacer(Modifier.height(12.dp))
            Text(
                text = "更新于 $timeText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreathingBall(bathroom: BathroomStatus, onClick: () -> Unit) {
    val ratio = bathroom.occupancyRatio.coerceIn(0f, 1f)
    val ballColor = ratioColor(ratio)
    // 粒子数随占用人数占比变化：空浴室 0 粒子，人越多越密
    val particleCount = remember(bathroom.useCount, bathroom.capacity) {
        val base = (ratio * 24f).roundToInt()
        base + if (bathroom.useCount > 0) 1 else 0
    }
    val particles = remember(bathroom.id, particleCount) {
        val rand = Random(bathroom.id * 31 + particleCount)
        List(particleCount) { i ->
            Particle(
                phase = rand.nextFloat() * 2f * PI.toFloat(),
                dist = 0.08f + rand.nextFloat() * 0.78f,
                radiusDp = 1.5f + rand.nextFloat() * 2.5f,
                alpha = 0.35f + rand.nextFloat() * 0.45f,
                twinkle = (i % 4) * PI.toFloat() / 2f
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift"
    )
    Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(104.dp)
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                }
                .clip(CircleShape)
                .background(ballColor)
                .clickable(onClick = onClick)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val c = center
                particles.forEach { p ->
                    val angle = p.phase + drift
                    val d = radius * p.dist
                    val twinkle = 0.75f + 0.25f * sin(drift * 3f + p.twinkle)
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha * twinkle),
                        radius = p.radiusDp.dp.toPx(),
                        center = Offset(c.x + cos(angle) * d, c.y + sin(angle) * d)
                    )
                }
            }
        }
    }
}

private data class Particle(
    val phase: Float,
    val dist: Float,
    val radiusDp: Float,
    val alpha: Float,
    val twinkle: Float
)

@Composable
private fun PlaceholderRow(title: String, hint: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun ratioColor(ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return if (r <= 0.6f) {
        lerp(EmptyColor, BusyColor, r / 0.6f)
    } else {
        lerp(BusyColor, FullColor, (r - 0.6f) / 0.4f)
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("重试") }
    }
}

@Composable
private fun LoadingPanel() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}





