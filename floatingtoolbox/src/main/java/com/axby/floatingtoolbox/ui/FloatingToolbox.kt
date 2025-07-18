package com.axby.floatingtoolbox.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.axby.floatingtoolbox.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private var defaultSections: List<Pair<Any, Color>> = mutableListOf(
    Pair(R.drawable.heart_ic, Color(0xFFfffba6)),
    Pair("section 2", Color(0xFF96d9ff)),
    Pair("section 3", Color(0xFF8093f1)),
    Pair(R.drawable.smiley_ic, Color(0xFFd5e2bc)),
    Pair("section 5", Color(0xFFdacabe)),
    Pair("section 6", Color(0xFFdacabe))
)

@Composable
fun FloatingToolbox(
    buttonPosition: MutableState<Offset>,
    showPopup: MutableState<Boolean>,
    donutSize: Dp = 160.dp,
    thickness: Dp = 85.dp,
    animationEnabled: Boolean = true,
    closeSectionColor: Color = Color(0xFFee6055),
    closeSectionIcon: ImageVector = Icons.Outlined.Close,
    closeSectionTint: Color = Color.White,
    centerButtonIcon: ImageVector? = Icons.Outlined.Edit,
    sectionIconSize: Dp = 20.dp,
    sectionLabelAndColorList: List<Pair<Any, Color>> = defaultSections.toMutableList()
        .apply {
            //this is the mandatory close button added to user added list of items at 0th position
            add(0, Pair<Any, Color>(closeSectionIcon, closeSectionColor))
        },
    onSectionClick: (sectionIndex: Int) -> Unit,
    onCenterClick: () -> Unit,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
    ),
    slicePopDelay: Long = 60L
) {

    if(!showPopup.value) return

    Popup(
        // this makes the popup occupy only the size of its content
        alignment = Alignment.TopStart, offset = IntOffset(
            // center horizontally: button.x - offset
            x = (buttonPosition.value.x - (donutSize.value + donutSize.div(3).value)).roundToInt(),
            // center vertically: button.y - offset
            y = (buttonPosition.value.y - (donutSize.value + donutSize.div(2).value)).roundToInt()
        ), onDismissRequest = { showPopup.value = false }) {
        SplitDonutMenu(
            modifier = Modifier.size(donutSize),
            sectionCount = sectionLabelAndColorList.size,
            colors = sectionLabelAndColorList,
            size = donutSize,
            thickness = thickness,
            onSectionClick = onSectionClick,
            onCenterClick = onCenterClick,
            showPopup = showPopup,
            animationEnabled = animationEnabled,
            animationSpec = animationSpec,
            slicePopDelay = slicePopDelay,
            centerButtonIcon = centerButtonIcon,
            closeSectionTint = closeSectionTint,
            iconSize = sectionIconSize
        )

    }
}

@Composable
private fun SplitDonutMenu(
    modifier: Modifier = Modifier,
    sectionCount: Int = 8,
    size: Dp = 120.dp,
    thickness: Dp = 35.dp,
    colors: List<Pair<Any, Color>> = defaultSections,
    onSectionClick: (sectionIndex: Int) -> Unit,
    centerButtonIcon: ImageVector?,
    closeSectionTint: Color,
    showPopup: MutableState<Boolean>,
    onCenterClick: () -> Unit,
    animationEnabled: Boolean,
    animationSpec: AnimationSpec<Float>,
    slicePopDelay: Long,
    iconSize: Dp = 40.dp
) {
    val sweep = 360f / sectionCount

    // Individual slice scale animations
    val sliceScales = remember { List(sectionCount) { Animatable(0f) } }

    // Build a painter for any Int resource IDs (null for strings)
    val painters: List<Painter?> = colors.map { (content, _) ->
        if (content is Int) painterResource(id = content) else null
    }


    LaunchedEffect(Unit) {
        sliceScales.forEachIndexed { index, animatable ->
            launch {
                // Stagger only when animations are enabled
                if (animationEnabled) {
                    delay(index * slicePopDelay)
                    animatable.animateTo(
                        targetValue = 1f, animationSpec = animationSpec
                    )
                } else {
                    // Instantly snap to the end state
                    animatable.snapTo(1f)
                }
            }
        }
    }

    Box(modifier = modifier
        .size(size)
        .clip(CircleShape)
        .pointerInput(sectionCount) {
            detectTapGestures { tapOffset ->
                val diameter = size.toPx()
                val center = Offset(diameter / 2, diameter / 2)
                val relative = tapOffset - center
                val distance = hypot(relative.x, relative.y)
                val outerRadius = diameter / 2
                val innerRadius = outerRadius / 2
                var angle = Math
                    .toDegrees(
                        atan2(relative.y.toDouble(), relative.x.toDouble())
                    )
                    .toFloat()
                angle = (angle + 112.5f + 360f) % 360f
                val index = (angle / sweep)
                    .toInt()
                    .coerceIn(0, sectionCount - 1)

                //detect if close section clicked
                if (index == 0) {
                    showPopup.value = false
                    return@detectTapGestures
                }

                // detect if center clicked
                if (distance < innerRadius || distance > outerRadius) {
                    onCenterClick().apply {
                        return@detectTapGestures
                    }
                }
                onSectionClick(index)
            }
        }) {

        Canvas(modifier = Modifier.size(size)) {
            //Shared geometry in px
            val diameterPx = size.toPx()
            val radius = diameterPx / 2f
            val strokePx = thickness.toPx()
            val center = Offset(radius, radius)

            //place text along the ring’s centerline
            val contentRadius = radius - (strokePx / 4f)

            //Prepare a TextPaint for measuring & drawing
            val textSizePx = 12.sp.toPx()
            val textPaint = android.text.TextPaint().apply {
                isAntiAlias = true
                textSize = textSizePx
                color = android.graphics.Color.WHITE
                textAlign = android.graphics.Paint.Align.CENTER
            }

            //Draw each slice and its curved label
            sliceScales.forEachIndexed { i, anim ->
                val sliceSweep = 360f / sectionCount

                // draw the grey + colored arcs
                withTransform({
                    rotate(degrees = i * sliceSweep, pivot = center)
                    scale(anim.value, anim.value, pivot = center)
                }) {
                    //this is rendered behind the arc with slightly larger size to give an appearance of outline
                    drawArc(
                        color = Color(0xFF9A9A9A),
                        startAngle = -(sliceSweep + 180f) / 2f,
                        sweepAngle = sliceSweep,
                        useCenter = false,
                        style = Stroke(width = strokePx + 4)
                    )
                    //the actual arc to be rendered
                    drawArc(
                        color = colors[i].second,
                        startAngle = -(sliceSweep + 180f) / 2f,
                        sweepAngle = sliceSweep,
                        useCenter = false,
                        style = Stroke(width = strokePx)
                    )
                }

                //match animation of the label content with the slices
                withTransform({ scale(anim.value, anim.value, pivot = center)}){

                    // match exactly the slice’s absolute start
                    val arcStart = i * sliceSweep + (-(sliceSweep + 180f) / 2f)
                    val arcSweep = sliceSweep

                    // build the arc‐path at textRadius
                    val rectF = android.graphics.RectF(
                        center.x - contentRadius,
                        center.y - contentRadius,
                        center.x + contentRadius,
                        center.y + contentRadius
                    )
                    val midAngle = (arcStart + arcSweep / 2f + 360f) % 360f
                    val rad = Math.toRadians(midAngle.toDouble()).toFloat()
                    val px = center.x + cos(rad) * contentRadius
                    val py = center.y + sin(rad) * contentRadius
                    val content = colors[i].first
                    if (i > 0 && content is String) {

                        //this flips the text of the bottom half for more readability
                        val path = android.graphics.Path().apply {
                            if (midAngle > 0f && midAngle < 180f) {
                                addArc(rectF, arcStart + arcSweep, -arcSweep)// bottom half
                            } else {
                                addArc(rectF, arcStart, arcSweep)// top & sides
                            }
                        }

                        // ellipsize if too long
                        val maxArcLen = (arcSweep / 360f) * (2 * Math.PI * contentRadius).toFloat()
                        val labelStr = if (textPaint.measureText(content) > maxArcLen) {
                            android.text.TextUtils.ellipsize(
                                content, textPaint, maxArcLen, android.text.TextUtils.TruncateAt.END
                            ).toString()
                        } else content

                        // draw the text along that center‐line path
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawTextOnPath(
                                labelStr,
                                path,
                                0f,
                                textSizePx / 2f,
                                textPaint
                            )
                        }
                    } else if (i > 0 && content is Int) {
                        // a painter was created above
                        painters[i]?.let { painter ->
                            val iconPx = iconSize.toPx()
                            // translate so draw() happens at (px - iconPx/2, py - iconPx/2)
                            translate(left = px - iconPx / 2f, top = py - iconPx / 2f) {
                                with(painter) {
                                    draw(size = Size(iconPx, iconPx))
                                }
                            }
                        }
                    }
                }

            }
        }


        // Overlay icon on 0th slice: position at top center
        Icon(imageVector = Icons.Outlined.Close,
            contentDescription = null,
            tint = closeSectionTint,
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = (thickness
                            .toPx()
                            .toInt()
                            .div(4)) - Icons.Outlined.Close.defaultWidth.value.toInt()
                    )
                })


        if (centerButtonIcon != null) {
            // Overlay icon at the center of the donut since it also has a clickable action
            Icon(imageVector = centerButtonIcon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopCenter)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = size.value.toInt() + centerButtonIcon.defaultWidth
                                .toPx()
                                .toInt()
                                .div(2)
                        )
                    }
                    .alpha(0.4f))
        }
    }
}

