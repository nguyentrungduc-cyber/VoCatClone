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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Chú mèo mascot đang ngồi (nét vẽ đơn giản, nhắm mắt) — dùng ở màn chào mừng.
 * Vẽ trên hệ toạ độ 140x150, co giãn theo chiều rộng của Canvas.
 */
@Composable
fun CatMascot(
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.onBackground,
    fillColor: Color = MaterialTheme.colorScheme.background
) {
    Canvas(
        modifier = modifier
            .size(width = 132.dp, height = 142.dp)
            .semantics { contentDescription = "Chú mèo mascot đang ngồi" }
    ) {
        val k = size.width / 140f
        fun p(x: Float, y: Float) = Offset(x * k, y * k)
        val stroke = Stroke(width = 3f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Đuôi (chỉ viền, không tô)
        val tail = Path().apply {
            moveTo(98f * k, 128f * k)
            quadraticBezierTo(130f * k, 132f * k, 124f * k, 104f * k)
            quadraticBezierTo(121f * k, 92f * k, 111f * k, 97f * k)
        }
        drawPath(path = tail, color = lineColor, style = stroke)

        // Thân
        val body = Path().apply {
            moveTo(48f * k, 76f * k)
            quadraticBezierTo(40f * k, 110f * k, 44f * k, 132f * k)
            lineTo(96f * k, 132f * k)
            quadraticBezierTo(100f * k, 110f * k, 92f * k, 76f * k)
        }
        drawPath(path = body, color = fillColor)
        drawPath(path = body, color = lineColor, style = stroke)
        drawLine(color = lineColor, start = p(70f, 110f), end = p(70f, 132f), strokeWidth = 3f * k, cap = StrokeCap.Round)

        // Hai bàn chân
        drawFilledOval(p(47f, 128.5f), Size(20f * k, 11f * k), fillColor, lineColor, stroke)
        drawFilledOval(p(73f, 128.5f), Size(20f * k, 11f * k), fillColor, lineColor, stroke)

        // Tai (vẽ trước đầu để đầu che phần chân tai)
        drawPolygon(listOf(p(40f, 50f), p(44f, 18f), p(64f, 36f)), fillColor, lineColor, stroke)
        drawPolygon(listOf(p(100f, 50f), p(96f, 18f), p(76f, 36f)), fillColor, lineColor, stroke)

        // Đầu, mắt nhắm, miệng
        drawFilledOval(p(36f, 30f), Size(68f * k, 56f * k), fillColor, lineColor, stroke)
        drawLine(color = lineColor, start = p(54f, 58f), end = p(62f, 58f), strokeWidth = 3f * k, cap = StrokeCap.Round)
        drawLine(color = lineColor, start = p(78f, 58f), end = p(86f, 58f), strokeWidth = 3f * k, cap = StrokeCap.Round)
        val mouth = Path().apply {
            moveTo(66f * k, 69f * k)
            quadraticBezierTo(70f * k, 73f * k, 74f * k, 69f * k)
        }
        drawPath(path = mouth, color = lineColor, style = Stroke(width = 2.5f * k, cap = StrokeCap.Round))
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200)
@Composable
private fun CatMascotPreview() {
    VoCatCloneTheme { CatMascot() }
}
