package com.example.paxradio.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
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
    state: LazyListState,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .filter { !it } // Only react when scroll is NOT in progress
            .map { 
                val center = state.layoutInfo.viewportEndOffset / 2
                state.layoutInfo.visibleItemsInfo
                    .minByOrNull { abs(it.offset + it.size / 2 - center) }
                    ?.index ?: -1
            }
            .distinctUntilChanged()
            .collect { index ->
                if (index != -1) {
                    onValueChange(index % items.size)
                    coroutineScope.launch {
                        state.animateScrollToItem(index)
                    }
                }
            }
    }

    LazyColumn(
        state = state,
        modifier = modifier.height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 40.dp)
    ) {
        items(Int.MAX_VALUE) { index ->
            val itemIndex = index % items.size
            val center = state.layoutInfo.viewportEndOffset / 2
            val itemInfo = state.layoutInfo.visibleItemsInfo.find { it.index == index }
            val itemOffset = itemInfo?.offset ?: 0
            val itemCenter = itemOffset + (itemInfo?.size ?: 0) / 2
            val distanceFromCenter = abs(itemCenter - center)

            val scale = 1f - (distanceFromCenter / center.toFloat() * 0.5f).coerceIn(0f, 1f)
            val alpha = 1f - (distanceFromCenter / center.toFloat() * 0.7f).coerceIn(0f, 1f)

            Text(
                text = items[itemIndex],
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
                    .alpha(alpha)
            )
        }
    }
}
