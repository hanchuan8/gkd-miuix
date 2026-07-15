package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.SmallTitle

@Composable
fun MenuGroupCard(inTop: Boolean = false, title: String, content: @Composable () -> Unit) {
    SmallTitle(
        text = title,
        modifier = Modifier.padding(top = if (inTop) 4.dp else 8.dp, bottom = 0.dp),
    )
    content()
}

@Composable
fun MenuItemCheckbox(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val actualOnClick = throttle(onClick)
    DropdownImpl(
        item = DropdownItem(text = text, enabled = enabled),
        optionSize = 8,
        isSelected = checked,
        index = 1,
        isFirst = false,
        isLast = false,
        enabled = enabled,
        onSelectedIndexChange = { actualOnClick() },
    )
}

@Composable
fun MenuItemCheckbox(
    text: String,
    stateFlow: MutableStateFlow<Boolean>,
    enabled: Boolean = true,
) = MenuItemCheckbox(
    text = text,
    checked = stateFlow.collectAsState().value,
    onClick = { stateFlow.update { !it } },
    enabled = enabled,
)

@Composable
fun MenuItemRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val actualOnClick = throttle(onClick)
    DropdownImpl(
        item = DropdownItem(text = text, enabled = enabled),
        optionSize = 8,
        isSelected = selected,
        index = 1,
        isFirst = false,
        isLast = false,
        enabled = enabled,
        onSelectedIndexChange = { actualOnClick() },
    )
}
