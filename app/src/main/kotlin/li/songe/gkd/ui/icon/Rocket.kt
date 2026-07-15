package li.songe.gkd.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** 工作模式入口：MIUIX 图标库无火箭，沿用 24dp 描边风格自定义矢量 */
val Rocket: ImageVector
    get() {
        if (_rocket != null) return _rocket!!
        _rocket = ImageVector.Builder(
            name = "Rocket",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 15f)
                curveToRelative(-0.83f, 0f, -1.58f, 0.34f, -2.12f, 0.88f)
                curveTo(2.7f, 17.06f, 2f, 22f, 2f, 22f)
                reflectiveCurveToRelative(4.94f, -0.7f, 6.12f, -1.88f)
                curveTo(8.66f, 19.58f, 9f, 18.83f, 9f, 18f)
                curveTo(9f, 16.34f, 7.66f, 15f, 6f, 15f)
                close()
                moveTo(6.71f, 18.71f)
                curveToRelative(-0.28f, 0.28f, -2.17f, 0.76f, -2.17f, 0.76f)
                reflectiveCurveToRelative(0.47f, -1.88f, 0.76f, -2.17f)
                curveTo(5.47f, 17.11f, 5.72f, 17f, 6f, 17f)
                curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
                curveTo(7f, 18.28f, 6.89f, 18.53f, 6.71f, 18.71f)
                close()
                moveTo(17.42f, 13.65f)
                curveToRelative(6.36f, -6.36f, 4.24f, -11.31f, 4.24f, -11.31f)
                reflectiveCurveToRelative(-4.95f, -2.12f, -11.31f, 4.24f)
                lineToRelative(-2.49f, -0.5f)
                curveTo(7.21f, 5.95f, 6.53f, 6.16f, 6.05f, 6.63f)
                lineTo(2f, 10.69f)
                lineToRelative(5f, 2.14f)
                lineToRelative(4.03f, 4.17f)
                lineToRelative(2.14f, 5f)
                lineToRelative(4.05f, -4.05f)
                curveToRelative(0.47f, -0.47f, 0.68f, -1.15f, 0.55f, -1.81f)
                lineToRelative(-0.5f, -2.49f)
                close()
                moveTo(7.41f, 10.83f)
                lineToRelative(-1.91f, -0.82f)
                lineToRelative(1.97f, -1.97f)
                lineToRelative(1.44f, 0.29f)
                curveTo(8.34f, 9.16f, 7.83f, 10.03f, 7.41f, 10.83f)
                close()
                moveTo(13.99f, 18.5f)
                lineToRelative(-0.82f, -1.91f)
                curveToRelative(0.8f, -0.42f, 1.67f, -0.93f, 2.49f, -1.5f)
                lineToRelative(0.29f, 1.44f)
                lineTo(13.99f, 18.5f)
                close()
                moveTo(16f, 12.24f)
                curveToRelative(-1.32f, 1.32f, -3.38f, 2.4f, -4.04f, 2.73f)
                lineToRelative(-2.93f, -2.93f)
                curveToRelative(0.32f, -0.65f, 1.4f, -2.71f, 2.73f, -4.04f)
                curveToRelative(4.68f, -4.68f, 8.23f, -3.99f, 8.23f, -3.99f)
                reflectiveCurveTo(20.68f, 7.56f, 16f, 12.24f)
                close()
                moveTo(15f, 11f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                close()
            }
        }.build()
        return _rocket!!
    }

@Suppress("ObjectPropertyName")
private var _rocket: ImageVector? = null
