package li.songe.gkd.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import li.songe.gkd.util.Option
import li.songe.gkd.util.OptionMenuLabel
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference

/**
 * 下拉选项。使用 [WindowDropdownPreference]，在全屏 Dialog / 普通页都能展开，
 * 避免 Overlay 挂在无 MIUIX Scaffold 的窗口里导致点击无反应。
 */
@Composable
fun <T> TextMenu(
    modifier: Modifier = Modifier,
    title: String,
    option: Option<T>,
    onOptionChange: ((Option<T>) -> Unit),
) {
    val items = remember(option.options) {
        option.options.map { other ->
            if (other is OptionMenuLabel) other.menuLabel else other.label
        }
    }
    val selectedIndex = remember(option.value, option.options) {
        option.options.indexOfFirst { it.value == option.value }.coerceAtLeast(0)
    }
    WindowDropdownPreference(
        modifier = modifier,
        title = title,
        items = items,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { index ->
            val selected = option.options.getOrNull(index) ?: return@WindowDropdownPreference
            if (selected != option) {
                onOptionChange(selected)
            }
        },
    )
}
