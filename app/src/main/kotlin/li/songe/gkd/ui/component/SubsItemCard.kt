package li.songe.gkd.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import li.songe.gkd.META
import li.songe.gkd.data.RawSubscription
import li.songe.gkd.data.SubsItem
import li.songe.gkd.ui.home.HomeVm
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.util.formatTimeAgo
import li.songe.gkd.util.mapState
import li.songe.gkd.util.subsLoadErrorsFlow
import li.songe.gkd.util.subsRefreshErrorsFlow
import li.songe.gkd.util.throttle
import li.songe.gkd.util.updateSubsMutex
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme


@Composable
fun SubsItemCard(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    subsItem: SubsItem,
    subscription: RawSubscription?,
    index: Int,
    isSelectedMode: Boolean,
    isSelected: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    onSelectedChange: (() -> Unit)? = null,
) {
    val mainVm = LocalMainViewModel.current
    val vm = viewModel<HomeVm>()
    val subsLoadError by remember(subsItem.id) {
        subsLoadErrorsFlow.mapState(vm.viewModelScope) { it[subsItem.id] }
    }.collectAsState()
    val subsRefreshError by remember(subsItem.id) {
        subsRefreshErrorsFlow.mapState(vm.viewModelScope) { it[subsItem.id] }
    }.collectAsState()
    val subsRefreshing by updateSubsMutex.state.collectAsState()
    val dragged by interactionSource.collectIsDraggedAsState()
    val onClick = {
        if (!dragged) {
            if (isSelectedMode) {
                onSelectedChange?.invoke()
            } else if (!updateSubsMutex.mutex.isLocked) {
                mainVm.sheetSubsIdFlow.value = subsItem.id
            }
        }
    }
    val containerColor = animateColorAsState(
        if (isSelected) {
            MiuixTheme.colorScheme.primaryContainer
        } else {
            MiuixTheme.colorScheme.surfaceContainer
        },
        tween()
    )
    Card(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .semantics {
                stateDescription = if (isSelectedMode) {
                    if (isSelected) "已选中" else "未选中"
                } else {
                    if (subsItem.enable) "已启用" else "已禁用"
                }
                this.onClick(label = "查看订阅详情", action = null)
                this.onLongClick(label = "进入多选模式", action = null)
            },
        colors = CardDefaults.defaultColors(
            color = containerColor.value
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (subscription != null) {
                    Text(
                        modifier = Modifier.semantics {
                            contentDescription = "订阅顺序：$index, 订阅名称 ${subscription.name}"
                        },
                        text = "$index. ${subscription.name}",
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = MiuixTheme.textStyles.body1,
                    )
                    Text(
                        text = subscription.numText,
                        style = MiuixTheme.textStyles.body2,
                        color = if (subscription.groupsSize == 0) {
                            LocalContentColor.current.copy(alpha = 0.5f)
                        } else {
                            LocalContentColor.current
                        }
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (subsItem.id >= 0) {
                            if (subscription.author != null) {
                                Text(
                                    modifier = Modifier.semantics {
                                        contentDescription = "作者 ${subscription.author}"
                                    },
                                    text = subscription.author,
                                    style = MiuixTheme.textStyles.footnote2,
                                )
                            }
                            Text(
                                modifier = Modifier.semantics {
                                    contentDescription = "订阅版本号 ${subscription.version}"
                                },
                                text = "v" + (subscription.version.toString()),
                                style = MiuixTheme.textStyles.footnote2,
                            )
                        } else {
                            Text(
                                modifier = Modifier.clearAndSetSemantics {},
                                text = META.appName,
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.secondary,
                            )
                        }
                        val timeStr = formatTimeAgo(subsItem.mtime)
                        Text(
                            modifier = Modifier.semantics {
                                contentDescription = "更新时间 $timeStr"
                            },
                            text = timeStr,
                            style = MiuixTheme.textStyles.footnote2,
                        )
                    }
                } else {
                    Text(
                        text = "id=${subsItem.id}",
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = MiuixTheme.textStyles.body2,
                    )
                    val color = if (subsLoadError != null) {
                        MiuixTheme.colorScheme.error
                    } else {
                        Color.Unspecified
                    }
                    Text(
                        text = subsLoadError?.message
                            ?: if (subsRefreshing) "加载中..." else "文件不存在",
                        style = MiuixTheme.textStyles.body2,
                        color = color
                    )
                }
                if (subsRefreshError != null) {
                    Text(
                        text = "更新错误: ${subsRefreshError?.message}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            val percent = usePercentAnimatable(!isSelectedMode)
            val switchModifier = Modifier.graphicsLayer(
                alpha = 0.5f + (1 - 0.5f) * percent.value,
            ).run {
                if (isSelectedMode) {
                    minimumInteractiveComponentSize()
                } else {
                    this
                }
            }
            PerfSwitch(
                key = subsItem.id,
                modifier = switchModifier,
                checked = subsItem.enable,
                onCheckedChange = if (isSelectedMode) null else throttle(fn = onCheckedChange),
            )
        }
    }
}
