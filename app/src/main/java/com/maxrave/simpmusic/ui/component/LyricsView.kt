package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.R
import com.maxrave.domain.data.model.streams.TimeLine
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.extension.KeepScreenOn
import com.maxrave.simpmusic.extension.animateScrollAndCentralizeItem
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.ui.theme.seed
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.NowPlayingScreenData
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun LyricsView(
    lyricsData: NowPlayingScreenData.LyricsData,
    timeLine: StateFlow<TimeLine>,
    onLineClick: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showScrollShadows: Boolean = false,
    backgroundColor: Color = Color(0xFF242424),
    hasBlurBackground: Boolean = false,
) {
    @Suppress("ktlint:standard:property-naming")
    val TAG = "LyricsView"

    val localDensity = LocalDensity.current

    var columnHeightDp by remember {
        mutableStateOf(0.dp)
    }
    var columnWidthDp by remember {
        mutableStateOf(0.dp)
    }
    var currentLineHeight by remember {
        mutableIntStateOf(0)
    }
    val listState = rememberLazyListState()
    val current by timeLine.collectAsStateWithLifecycle()
    var currentLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }


    val showTopShadow by remember {
        derivedStateOf {
            !hasBlurBackground && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)
        }
    }
    val showBottomShadow by remember {
        derivedStateOf {
            if (hasBlurBackground) {
                false
            } else {
                val layoutInfo = listState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                if (lastVisibleItem != null) {
                    lastVisibleItem.index < layoutInfo.totalItemsCount - 1 ||
                        lastVisibleItem.offset + lastVisibleItem.size > layoutInfo.viewportEndOffset
                } else {
                    false
                }
            }
        }
    }

    LaunchedEffect(key1 = current) {
        val lines = lyricsData.lyrics.lines
        if (current.current > 0L) {
            lines?.indices?.forEach { i ->
                val sentence = lines[i]
                val startTimeMs = sentence.startTimeMs.toLong()

                // estimate the end time of the current sentence based on the start time of the next sentence
                val endTimeMs =
                    if (i < lines.size - 1) {
                        lines[i + 1].startTimeMs.toLong()
                    } else {
                        // if this is the last sentence, set the end time to be some default value (e.g., 1 minute after the start time)
                        startTimeMs + 60000
                    }
                if (current.current in startTimeMs..endTimeMs) {
                    currentLineIndex = i
                }
            }
            if (!lines.isNullOrEmpty() &&
                (
                    current.current in (
                        0..(
                            lines.getOrNull(0)?.startTimeMs
                                ?: "0"
                            ).toLong()
                        )
                    )
            ) {
                currentLineIndex = -1
            }
        } else {
            currentLineIndex = -1
        }
    }
    LaunchedEffect(key1 = currentLineIndex, key2 = currentLineHeight) {
        if (currentLineIndex > -1 && currentLineHeight > 0 && lyricsData.lyrics.syncType == "LINE_SYNCED") {
            val boxEnd = listState.layoutInfo.viewportEndOffset
            val boxStart = listState.layoutInfo.viewportStartOffset
            val viewPort = boxEnd - boxStart
            val offset = viewPort / 2 - currentLineHeight / 2
            Logger.w(TAG, "Offset: $offset")
            listState.animateScrollAndCentralizeItem(
                index = currentLineIndex,
                this,
            )
        }
    }


    fun findClosestTranslatedLine(originalTimeMs: String): String? {
        val translatedLines = lyricsData.translatedLyrics?.lines ?: return null
        if (translatedLines.isEmpty()) return null

        val originalTime = originalTimeMs.toLongOrNull() ?: return null


        return translatedLines
            .minByOrNull {
                abs((it.startTimeMs.toLongOrNull() ?: 0L) - originalTime)
            }?.let {
                val abs = abs((it.startTimeMs.toLongOrNull() ?: 0L) - originalTime)
                if (abs < 1000L) {
                    it
                } else {
                    null
                }
            }?.words
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    columnHeightDp = with(localDensity) { coordinates.size.height.toDp() }
                    columnWidthDp = with(localDensity) { coordinates.size.width.toDp() }
                }
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    // Only show scroll shadows if enabled AND no blur background
                    if (showScrollShadows && !hasBlurBackground) {
                        // Top shadow
                        if (showTopShadow) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        backgroundColor,
                                        backgroundColor.copy(alpha = 0.8f),
                                        backgroundColor.copy(alpha = 0.4f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = 80.dp.toPx()
                                ),
                                topLeft = Offset(0f, 0f),
                                size = Size(size.width, 80.dp.toPx())
                            )
                        }

                        // Bottom shadow
                        if (showBottomShadow) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        backgroundColor.copy(alpha = 0.4f),
                                        backgroundColor.copy(alpha = 0.8f),
                                        backgroundColor
                                    ),
                                    startY = size.height - 80.dp.toPx(),
                                    endY = size.height
                                ),
                                topLeft = Offset(0f, size.height - 80.dp.toPx()),
                                size = Size(size.width, 80.dp.toPx())
                            )
                        }
                    }
                },
        ) {
            items(lyricsData.lyrics.lines?.size ?: 0) { index ->
                val line = lyricsData.lyrics.lines?.getOrNull(index)
                // Tìm translated lyrics phù hợp dựa vào thời gian
                val translatedWords = line?.startTimeMs?.let { findClosestTranslatedLine(it) }
                Log.d(TAG, "Line $index: ${line?.words}, Translated: $translatedWords")

                line?.words?.let {
                    LyricsLineItem(
                        originalWords = it,
                        translatedWords = translatedWords,
                        isBold = index <= currentLineIndex,
                        modifier =
                            Modifier
                                .clickable {
                                    onLineClick(line.startTimeMs.toFloat() * 100 / timeLine.value.total)
                                }.onGloballyPositioned { c ->
                                    currentLineHeight = c.size.height
                                },
                    )
                }
            }
        }
    }
}

@Composable
fun LyricsLineItem(
    originalWords: String,
    translatedWords: String?,
    isBold: Boolean,
    modifier: Modifier = Modifier,
) {
    Crossfade(targetState = isBold) {
        if (it) {
            Column(
                modifier = modifier,
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = originalWords, style = typo.headlineMedium, color = Color.White)
                if (translatedWords != null) {
                    Text(text = translatedWords, style = typo.bodyMedium, color = Color.Yellow)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
    if (!isBold) {
        Column(
            modifier = modifier,
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = originalWords,
                style = typo.bodyLarge,
                color =
                    Color.LightGray.copy(
                        alpha = 0.8f,
                    ),
            )
            if (translatedWords != null) {
                Text(
                    text = translatedWords,
                    style = typo.bodyMedium,
                    color =
                        Color(0xFF97971A).copy(
                            alpha = 0.8f,
                        ),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun FullscreenLyricsSheet(
    sharedViewModel: SharedViewModel,
    navController: NavController,
    color: Color = Color(0xFF242424),
    shouldHaze: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val screenDataState by sharedViewModel.nowPlayingScreenData.collectAsStateWithLifecycle()
    val timelineState by sharedViewModel.timeline.collectAsStateWithLifecycle()
    val controllerState by sharedViewModel.controllerState.collectAsStateWithLifecycle()

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val windowInsets = WindowInsets.systemBars

    var sliderValue by rememberSaveable {
        mutableFloatStateOf(0f)
    }

    // Auto-hide controls state - Only hide control buttons, not title/progress
    var showControlButtons by rememberSaveable {
        mutableStateOf(true)
    }

    // Reset auto-hide timer when controls are shown
    LaunchedEffect(key1 = showControlButtons) {
        if (showControlButtons) {
            delay(4000) // Hide after 4 seconds
            showControlButtons = false
        }
    }

    LaunchedEffect(key1 = timelineState) {
        sliderValue =
            if (timelineState.total > 0L) {
                timelineState.current.toFloat() * 100 / timelineState.total.toFloat()
            } else {
                0f
            }
    }

    if (screenDataState.lyricsData != null) {
        KeepScreenOn()
    }

    var showQueueBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showInfoBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        containerColor = color,
        contentColor = Color.Transparent,
        dragHandle = {},
        scrimColor = Color.Black.copy(alpha = .5f),
        sheetState = sheetState,
        modifier =
            Modifier
                .fillMaxHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Show controls on tap
                    showControlButtons = true
                },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RectangleShape,
    ) {
        Box {
            val hazeState =
                rememberHazeState(
                    blurEnabled = true,
                )
            if (shouldHaze) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                ) {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(screenDataState.thumbnailURL)
                                .crossfade(300)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .diskCacheKey(screenDataState.thumbnailURL)
                                .build(),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                shape = RectangleShape,
                colors = CardDefaults.cardColors().copy(containerColor = if (shouldHaze) Color.Transparent else color),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (shouldHaze) {
                                Modifier.hazeEffect(
                                    hazeState,
                                    style = CupertinoMaterials.thin(),
                                ) {
                                    blurEnabled = true
                                }
                            } else {
                                Modifier
                            }
                        )
                        .padding(
                            bottom = with(localDensity) {
                                windowInsets.getBottom(localDensity).toDp()
                            },
                            top = with(localDensity) {
                                windowInsets.getTop(localDensity).toDp()
                            },
                        )
                ) {
                    // Top App Bar - Always visible
                    TopAppBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        colors =
                            TopAppBarDefaults.topAppBarColors().copy(
                                containerColor = Color.Transparent,
                            ),
                        title = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = stringResource(id = R.string.now_playing_upper),
                                    style = typo.bodyMedium,
                                    color = Color.White,
                                )
                                Text(
                                    text = screenDataState.nowPlayingTitle,
                                    style = typo.labelMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_keyboard_arrow_down_24),
                                    contentDescription = "",
                                    tint = Color.White,
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {}, modifier = Modifier.alpha(0f)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_more_vert_24),
                                    contentDescription = "",
                                    tint = Color.White,
                                )
                            }
                        },
                    )

                    // Lyrics Content - Expands when controls are hidden
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 50.dp)
                    ) {
                        Crossfade(
                            targetState = screenDataState.lyricsData != null,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (it) {
                                screenDataState.lyricsData?.let { lyrics ->
                                    LyricsView(
                                        lyricsData = lyrics,
                                        timeLine = sharedViewModel.timeline,
                                        onLineClick = { f ->
                                            sharedViewModel.onUIEvent(UIEvent.UpdateProgress(f))
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        showScrollShadows = true,
                                        backgroundColor = color,
                                        hasBlurBackground = shouldHaze // Pass blur background state
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.unavailable),
                                        style = typo.bodyMedium,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    // Progress Bar and Time - Always visible, positioned based on control buttons visibility
                    Column {
                        // Real Slider
                        Box(
                            Modifier
                                .padding(
                                    top = 15.dp,
                                ).padding(horizontal = 40.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Crossfade(timelineState.loading) {
                                    if (it) {
                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                            LinearProgressIndicator(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .padding(
                                                            horizontal = 3.dp,
                                                        ).clip(
                                                            RoundedCornerShape(8.dp),
                                                        ),
                                                color = Color.Gray,
                                                trackColor = Color.DarkGray,
                                                strokeCap = StrokeCap.Round,
                                            )
                                        }
                                    } else {
                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                            LinearProgressIndicator(
                                                progress = { timelineState.bufferedPercent.toFloat() / 100 },
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .padding(
                                                            horizontal = 3.dp,
                                                        ).clip(
                                                            RoundedCornerShape(8.dp),
                                                        ),
                                                color = Color.Gray,
                                                trackColor = Color.DarkGray,
                                                strokeCap = StrokeCap.Round,
                                                drawStopIndicator = {},
                                            )
                                        }
                                    }
                                }
                            }
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                Slider(
                                    value = sliderValue,
                                    onValueChange = {
                                        sharedViewModel.onUIEvent(
                                            UIEvent.UpdateProgress(it),
                                        )
                                    },
                                    valueRange = 0f..100f,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 3.dp)
                                            .align(
                                                Alignment.TopCenter,
                                            ),
                                    track = { sliderState ->
                                        SliderDefaults.Track(
                                            modifier =
                                                Modifier
                                                    .height(5.dp),
                                            enabled = true,
                                            sliderState = sliderState,
                                            colors =
                                                SliderDefaults.colors().copy(
                                                    thumbColor = Color.White,
                                                    activeTrackColor = Color.White,
                                                    inactiveTrackColor = Color.Transparent,
                                                ),
                                            thumbTrackGapSize = 0.dp,
                                            drawTick = { _, _ -> },
                                            drawStopIndicator = null,
                                        )
                                    },
                                    thumb = {
                                        SliderDefaults.Thumb(
                                            modifier =
                                                Modifier
                                                    .height(18.dp)
                                                    .width(8.dp)
                                                    .padding(
                                                        vertical = 4.dp,
                                                    ),
                                            thumbSize = DpSize(8.dp, 8.dp),
                                            interactionSource =
                                                remember {
                                                    MutableInteractionSource()
                                                },
                                            colors =
                                                SliderDefaults.colors().copy(
                                                    thumbColor = Color.White,
                                                    activeTrackColor = Color.White,
                                                    inactiveTrackColor = Color.Transparent,
                                                ),
                                            enabled = true,
                                        )
                                    },
                                )
                            }
                        }
                        // Time Layout
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp),
                        ) {
                            Text(
                                text = formatDuration(timelineState.current, context),
                                style = typo.bodyMedium,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Left,
                            )
                            Text(
                                text = formatDuration(timelineState.total, context),
                                style = typo.bodyMedium,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right,
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(5.dp),
                        )
                    }

                    // Control Buttons - Animated visibility
                    AnimatedVisibility(
                        visible = showControlButtons,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        Column {
                            // Control Button Layout
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                        .padding(horizontal = 20.dp),
                            ) {
                                FilledTonalIconButton(
                                    colors =
                                        IconButtonDefaults.iconButtonColors().copy(
                                            containerColor = Color.Transparent,
                                        ),
                                    modifier =
                                        Modifier
                                            .weight(1f) // Distribute equal weight
                                            .aspectRatio(1f)
                                            .clip(
                                                CircleShape,
                                            ),
                                    onClick = {
                                        sharedViewModel.onUIEvent(UIEvent.Shuffle)
                                        showControlButtons = true // Reset timer on interaction
                                    },
                                ) {
                                    Crossfade(targetState = controllerState.isShuffle, label = "Shuffle Button") { isShuffle ->
                                        if (isShuffle) {
                                            Icon(
                                                imageVector = Icons.Rounded.Shuffle,
                                                tint = seed, // Accent color when shuffle is ON
                                                contentDescription = "",
                                                modifier = Modifier.size(32.dp),
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.Shuffle,
                                                tint = Color.White, // White when shuffle is OFF
                                                contentDescription = "",
                                                modifier = Modifier.size(32.dp),
                                            )
                                        }
                                    }
                                }
                                FilledTonalIconButton(
                                    colors =
                                        IconButtonDefaults.iconButtonColors().copy(
                                            containerColor = Color.Transparent,
                                        ),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(
                                                CircleShape,
                                            ),
                                    onClick = {
                                        if (controllerState.isPreviousAvailable) {
                                            sharedViewModel.onUIEvent(UIEvent.Previous)
                                            showControlButtons = true // Reset timer on interaction
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipPrevious,
                                        tint = if (controllerState.isPreviousAvailable) Color.White else Color.Gray,
                                        contentDescription = "",
                                        modifier = Modifier.size(52.dp),
                                    )
                                }
                                FilledTonalIconButton(
                                    colors =
                                        IconButtonDefaults.iconButtonColors().copy(
                                            containerColor = Color.Transparent,
                                        ),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(
                                                CircleShape,
                                            ),
                                    onClick = {
                                        sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                        showControlButtons = true // Reset timer on interaction
                                    },
                                ) {
                                    Crossfade(targetState = controllerState.isPlaying) { isPlaying ->
                                        if (!isPlaying) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayCircle,
                                                tint = Color.White,
                                                contentDescription = "",
                                                modifier = Modifier.size(72.dp),
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.PauseCircle,
                                                tint = Color.White,
                                                contentDescription = "",
                                                modifier = Modifier.size(72.dp),
                                            )
                                        }
                                    }
                                }
                                FilledTonalIconButton(
                                    colors =
                                        IconButtonDefaults.iconButtonColors().copy(
                                            containerColor = Color.Transparent,
                                        ),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(
                                                CircleShape,
                                            ),
                                    onClick = {
                                        if (controllerState.isNextAvailable) {
                                            sharedViewModel.onUIEvent(UIEvent.Next)
                                            showControlButtons = true // Reset timer on interaction
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        tint = if (controllerState.isNextAvailable) Color.White else Color.Gray,
                                        contentDescription = "",
                                        modifier = Modifier.size(52.dp),
                                    )
                                }
                                FilledTonalIconButton(
                                    colors =
                                        IconButtonDefaults.iconButtonColors().copy(
                                            containerColor = Color.Transparent,
                                        ),
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(
                                                CircleShape,
                                            ),
                                    onClick = {
                                        sharedViewModel.onUIEvent(UIEvent.Repeat)
                                        showControlButtons = true // Reset timer on interaction
                                    },
                                ) {
                                    Crossfade(targetState = controllerState.repeatState) { rs ->
                                        when (rs) {
                                            is RepeatState.None -> {
                                                Icon(
                                                    imageVector = Icons.Rounded.Repeat,
                                                    tint = Color.White,
                                                    contentDescription = "",
                                                    modifier = Modifier.size(32.dp),
                                                )
                                            }

                                            RepeatState.All -> {
                                                Icon(
                                                    imageVector = Icons.Rounded.Repeat,
                                                    tint = seed,
                                                    contentDescription = "",
                                                    modifier = Modifier.size(32.dp),
                                                )
                                            }

                                            RepeatState.One -> {
                                                Icon(
                                                    imageVector = Icons.Rounded.RepeatOne,
                                                    tint = seed,
                                                    contentDescription = "",
                                                    modifier = Modifier.size(32.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            // List Bottom Buttons
                            // 24.dp
                            Box(
                                modifier =
                                    Modifier
                                        .height(32.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 40.dp),
                            ) {
                                IconButton(
                                    modifier =
                                        Modifier
                                            .size(24.dp)
                                            .aspectRatio(1f)
                                            .align(Alignment.CenterStart)
                                            .clip(
                                                CircleShape,
                                            ),
                                    onClick = {
                                        showInfoBottomSheet = true
                                        showControlButtons = true // Reset timer on interaction
                                    },
                                ) {
                                    Icon(imageVector = Icons.Outlined.Info, tint = Color.White, contentDescription = "")
                                }
                                Row(
                                    Modifier.align(Alignment.CenterEnd),
                                ) {
                                    Spacer(modifier = Modifier.size(8.dp))
                                    IconButton(
                                        modifier =
                                            Modifier
                                                .size(24.dp)
                                                .aspectRatio(1f)
                                                .clip(
                                                    CircleShape,
                                                ),
                                        onClick = {
                                            showQueueBottomSheet = true
                                            showControlButtons = true // Reset timer on interaction
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                                            tint = Color.White,
                                            contentDescription = "",
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    // When control buttons are hidden, add spacer to maintain proper spacing
                    if (!showControlButtons) {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
    if (showQueueBottomSheet) {
        QueueBottomSheet(
            onDismiss = {
                showQueueBottomSheet = false
            },
        )
    }
    if (showInfoBottomSheet) {
        InfoPlayerBottomSheet(
            onDismiss = {
                showInfoBottomSheet = false
            },
        )
    }
}
