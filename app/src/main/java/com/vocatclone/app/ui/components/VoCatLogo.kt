package com.vocatclone.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/** Vẽ một đa giác (dùng cho tai mèo): tô màu nền trước để che nét cũ, rồi vẽ viền. */
internal fun DrawScope.drawPolygon(points: List<Offset>, fill: Color, line: Color, stroke: Stroke) {
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path = path, color = fill)
    drawPath(path = path, color = line, style = stroke)
}

/** Vẽ hình elip (đầu, bàn chân): tô nền rồi vẽ viền. */
internal fun DrawScope.drawFilledOval(topLeft: Offset, size: Size, fill: Color, line: Color, stroke: Stroke) {
    drawOval(color = fill, topLeft = topLeft, size = size)
    drawOval(color = line, topLeft = topLeft, size = size, style = stroke)
}

/**
 * Logo VoCat Clone: vòng tròn bao quanh đầu mèo nhắm mắt.
 * Vẽ bằng Canvas trên hệ toạ độ 160x160 rồi co giãn theo [logoSize], nên không cần file ảnh.
 */
@Composable
fun VoCatLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 144.dp,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    lineColor: Color = MaterialTheme.colorScheme.onBackground,
    fillColor: Color = MaterialTheme.colorScheme.background
) {
    Canvas(
        modifier = modifier
            .size(logoSize)
            .semantics { contentDescription = "Logo VoCat Clone" }
    ) {
        val k = size.minDimension / 160f                       // tỉ lệ từ toạ độ 160 sang pixel
        fun p(x: Float, y: Float) = Offset(x * k, y * k)
        val stroke = Stroke(width = 4f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)

        drawCircle(color = ringColor, radius = 70f * k, center = p(80f, 80f), style = stroke)
        drawPolygon(listOf(p(50f, 70f), p(54f, 38f), p(78f, 56f)), fillColor, lineColor, stroke)    // tai trái
        drawPolygon(listOf(p(110f, 70f), p(106f, 38f), p(82f, 56f)), fillColor, lineColor, stroke)  // tai phải
        drawFilledOval(p(42f, 54f), Size(76f * k, 64f * k), fillColor, lineColor, stroke)           // đầu
        drawLine(color = lineColor, start = p(62f, 84f), end = p(74f, 84f), strokeWidth = 4f * k, cap = StrokeCap.Round)  // mắt trái nhắm
        drawLine(color = lineColor, start = p(86f, 84f), end = p(98f, 84f), strokeWidth = 4f * k, cap = StrokeCap.Round)  // mắt phải nhắm
        val mouth = Path().apply {
            moveTo(75f * k, 98f * k)
            quadraticBezierTo(80f * k, 103f * k, 85f * k, 98f * k)
        }
        drawPath(path = mouth, color = lineColor, style = Stroke(width = 3.5f * k, cap = StrokeCap.Round))
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200)
@Composable
private fun VoCatLogoPreview() {
    VoCatCloneTheme { VoCatLogo() }
}
