package li.songe.gkd.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import li.songe.gkd.META
import li.songe.gkd.ui.ActionLogRoute
import li.songe.gkd.ui.SubsAppListRoute
import li.songe.gkd.ui.SubsCategoryRoute
import li.songe.gkd.ui.SubsGlobalGroupListRoute
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.util.LOCAL_SUBS_ID
import li.songe.gkd.util.checkSubsUpdate
import li.songe.gkd.util.deleteSubscription
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.launchTry
import li.songe.gkd.util.subsItemsFlow
import li.songe.gkd.util.subsMapFlow
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toast
import li.songe.gkd.util.updateSubsMutex
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
fun SubsSheet(
    vm: ViewModel,
    sheetSubsIdFlow: MutableStateFlow<Long?>,
) {
    val mainVm = LocalMainViewModel.current
    val subsItems by subsItemsFlow.collectAsState()
    val sheetId by sheetSubsIdFlow.collectAsState()
    val subsIdToRaw by subsMapFlow.collectAsState()
    val subsItem = sheetId?.let { id -> subsItems.find { it.id == id } }
    val subscription = sheetId?.let { subsIdToRaw[it] }
    val show = sheetId != null && subsItem != null
    val showName = subscription?.name ?: subsItem?.let { "id=${it.id}" }.orEmpty()

    WindowBottomSheet(
        show = show,
        title = showName.ifEmpty { null },
        onDismissRequest = { sheetSubsIdFlow.value = null },
        enableNestedScroll = true,
    ) {
        if (subsItem == null) return@WindowBottomSheet
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (subscription != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clearAndSetSemantics {
                                contentDescription =
                                    "作者：${subscription.author ?: "未知"}, 版本号：v${subscription.version}, 更新时间：${subsItem.mtimeStr}"
                            },
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "作者",
                                style = MiuixTheme.textStyles.headline2,
                            )
                            Text(
                                text = "v${subscription.version}",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MiuixTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            if (!subsItem.isLocal) {
                                Text(
                                    text = subscription.author ?: "未知",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(
                                        alpha = if (subscription.author == null) 0.5f else 1f,
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            } else {
                                Text(
                                    text = META.appName,
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.secondary,
                                )
                            }
                            Text(
                                text = subsItem.mtimeStr,
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    if (subscription.globalGroups.isNotEmpty() || subsItem.isLocal) {
                        BasicComponent(
                            title = "全局规则",
                            summary = if (subscription.globalGroups.isNotEmpty()) {
                                "共 ${subscription.globalGroups.size} 全局规则"
                            } else {
                                "暂无"
                            },
                            onClick = throttle {
                                sheetSubsIdFlow.value = null
                                mainVm.navigatePage(SubsGlobalGroupListRoute(subsItem.id))
                            },
                            endActions = {
                                PerfIcon(imageVector = PerfIcon.KeyboardArrowRight)
                            },
                        )
                    }
                    if (subscription.appGroups.isNotEmpty() || subsItem.isLocal) {
                        BasicComponent(
                            title = "应用规则",
                            summary = if (subscription.appGroups.isNotEmpty()) {
                                "共 ${subscription.apps.size} 应用 ${subscription.appGroups.size} 规则"
                            } else {
                                "暂无"
                            },
                            onClick = throttle {
                                sheetSubsIdFlow.value = null
                                mainVm.navigatePage(SubsAppListRoute(subsItem.id))
                            },
                            endActions = {
                                PerfIcon(imageVector = PerfIcon.KeyboardArrowRight)
                            },
                        )
                    }
                    if (subscription.categories.isNotEmpty() || subsItem.isLocal) {
                        BasicComponent(
                            title = "规则类别",
                            summary = if (subscription.categories.isNotEmpty()) {
                                "共 ${subscription.categories.size} 类别"
                            } else {
                                "暂无"
                            },
                            onClick = throttle {
                                sheetSubsIdFlow.value = null
                                mainVm.navigatePage(SubsCategoryRoute(subsItem.id))
                            },
                            endActions = {
                                PerfIcon(imageVector = PerfIcon.KeyboardArrowRight)
                            },
                        )
                    }
                    if (!subsItem.isLocal && subsItem.updateUrl != null) {
                        BasicComponent(
                            title = "订阅链接",
                            summary = subsItem.updateUrl,
                            onClick = throttle {
                                if (updateSubsMutex.mutex.isLocked) {
                                    toast("正在刷新订阅,请稍后操作")
                                    return@throttle
                                }
                                mainVm.viewModelScope.launchTry {
                                    val url = mainVm.inputSubsLinkOption.getResult(
                                        initValue = subsItem.updateUrl,
                                    ) ?: return@launchTry
                                    mainVm.addOrModifySubs(url, subsItem)
                                }
                            },
                            endActions = {
                                PerfIcon(imageVector = PerfIcon.Edit)
                            },
                        )
                    }
                }
            } else {
                val loading by updateSubsMutex.state.collectAsState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (loading) {
                        InfiniteProgressIndicator()
                    } else {
                        Text(
                            text = "文件加载错误或不存在",
                            style = MiuixTheme.textStyles.headline2,
                            color = MiuixTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            text = "重新加载",
                            onClick = throttle { checkSubsUpdate(showToast = true) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!subsItem.isLocal && subscription?.supportUri != null) {
                    PerfIconButton(
                        imageVector = PerfIcon.HelpOutline,
                        onClick = throttle {
                            mainVm.textFlow.value = subscription.supportUri
                        },
                    )
                }
                PerfIconButton(
                    imageVector = PerfIcon.History,
                    onClick = throttle {
                        sheetSubsIdFlow.value = null
                        mainVm.navigatePage(ActionLogRoute(subsId = subsItem.id))
                    },
                )
                if (subsItem.id != LOCAL_SUBS_ID) {
                    PerfIconButton(
                        imageVector = PerfIcon.Delete,
                        onClick = throttle(
                            vm.viewModelScope.launchAsFn {
                                mainVm.dialogFlow.waitResult(
                                    title = "删除订阅",
                                    text = "确定删除 ${subscription?.name ?: subsItem.id} ?",
                                    error = true,
                                )
                                sheetSubsIdFlow.value = null
                                deleteSubscription(subsItem.id)
                            }
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(EmptyHeight / 2))
        }
    }
}
