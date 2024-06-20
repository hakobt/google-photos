package com.hakobt.photosscroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hakobt.photosscroll.ui.theme.PhotoScrollTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoScrollTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    PhotoHomeScreen(paddingValues = paddingValues)
                }
            }
        }
    }
}

@Composable
fun PhotoHomeScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        PhotoScrollView(photos = images + images + images + images)
        PhotoGridView(photos = images + images + images + images)
    }
}

@Composable
fun PhotoGridView(photos: List<String>) {
    val gridType = remember {
        mutableStateOf(StaggeredGridCells.Fixed(5))
    }
    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(vertical = 24.dp),
        columns = gridType.value,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalItemSpacing = 2.dp
    ) {
        items(photos) {
            AsyncImage(
                modifier = Modifier
                    .clipToBounds()
                    .aspectRatio(1f),
                model = it,
                contentScale = ContentScale.Crop,
                contentDescription = ""
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoScrollView(photos: List<String>) {
    val state = rememberLazyListState()
    val visibleItems by remember {
        snapshotFlow {
            val layoutInfo = state.layoutInfo
            layoutInfo.visibleItemsInfo.mapIndexed { index, it ->
                val cutFromLeft = (layoutInfo.viewportStartOffset - it.offset).coerceAtLeast(0)
                val cutFromRight =
                    (it.offset + it.size - layoutInfo.viewportEndOffset).coerceAtLeast(0)
                Triple(it, cutFromLeft.toFloat(), cutFromRight.toFloat())
            }
        }
    }.collectAsState(emptyList())
    LazyRow(
        modifier = Modifier
            .fillMaxHeight(0.2f)
            .fillMaxWidth(),
        state = state,
        verticalAlignment = Alignment.Top
    ) {
        stickyHeader {  }
        itemsIndexed(photos, { index, item -> index }) { index, item ->
            val firstVisibleItem = visibleItems.firstOrNull { it.first.key == index }

            val cutFromLeft = firstVisibleItem?.second ?: 0f
            val cutFromRight = firstVisibleItem?.third ?: 0f
            PhotoView(
                url = item,
                index = index,
                cutFromLeft = cutFromLeft,
                cutFromRight = cutFromRight
            )
        }
    }
}


@Composable
fun PhotoView(
    url: String,
    index: Int,
    cutFromLeft: Float,
    cutFromRight: Float
) {
    val fractionVisible = 1 - abs(cutFromLeft - cutFromRight) / 120

    val fraction = (cutFromLeft - cutFromRight) / 120

    val blurAmount = remember { mutableFloatStateOf(8f) }

    val alpha = FastOutSlowInEasing.transform(fractionVisible)
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .height(160.dp)
            .width(120.dp)
            .clickable {
                blurAmount.floatValue = 0f
            }
            .clip(RoundedCornerShape(16.dp)),
    ) {
        AsyncImage(
            modifier = Modifier
                .blur(blurAmount.floatValue.dp)
                .height(160.dp)
                .width(360.dp)
                .absoluteOffset(-((cutFromRight - cutFromLeft) * 0.1).dp),
            model = url,
            contentDescription = "",
            contentScale = ContentScale.Crop,
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .clipToBounds()
                .align(Alignment.BottomCenter)
                .alpha(alpha),
            color = Color.White,
            textAlign = TextAlign.Start,
            text = "Image $index"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PhotoScrollView(photos = images)
}