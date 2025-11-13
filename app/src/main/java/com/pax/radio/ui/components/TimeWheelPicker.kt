package com.pax.radio.ui.components

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun TimeWheelPicker(
    items: List<String>,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 48.dp,
    visibleItemsCount: Int = 3,
    initialIndex: Int = 0,
    onValueChange: (Int) -> Unit
) {
    if (items.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val middleIndex = Int.MAX_VALUE / 2
    val startIndex = middleIndex - (middleIndex % items.size) + initialIndex
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    var lastReportedIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .map {
                val center = listState.layoutInfo.viewportSize.height / 2
                listState.layoutInfo.visibleItemsInfo
                    .minByOrNull { item -> abs(item.offset + item.size / 2 - center) }
            }
            .distinctUntilChanged()
            .collect { closestItem ->
                if (closestItem != null) {
                    val center = listState.layoutInfo.viewportSize.height / 2
                    val offset = closestItem.offset + closestItem.size / 2 - center
                    coroutineScope.launch {
                        listState.animateScrollToItem(closestItem.index, -offset)
                    }

                    val realIndex = closestItem.index % items.size
                    if (lastReportedIndex != realIndex) {
                        onValueChange(realIndex)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        lastReportedIndex = realIndex
                    }
                }
            }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = ScrollableDefaults.flingBehavior(),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.matchParentSize()
        ) {
            items(Int.MAX_VALUE) { virtualIndex ->
                val realIndex = virtualIndex % items.size
                val item = items[realIndex]

                Text(
                    text = item,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .height(itemHeight)
                        .graphicsLayer {
                            val viewportHeight = listState.layoutInfo.viewportSize.height.toFloat()
                            if (viewportHeight == 0f) return@graphicsLayer

                            val center = viewportHeight / 2f
                            val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == virtualIndex }
                            val itemCenter = itemInfo?.let { it.offset + it.size / 2f } ?: center

                            val distance = abs(center - itemCenter)
                            val maxDistance = center

                            val scale = 1f - (distance / maxDistance * 0.7f).coerceIn(0f, 0.7f)
                            alpha = 1f - (distance / maxDistance * 0.8f).coerceIn(0f, 0.8f)
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}
