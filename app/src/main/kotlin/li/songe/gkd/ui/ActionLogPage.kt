package li.songe.gkd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import li.songe.gkd.data.ActionLog
import li.songe.gkd.data.AppInfo
import li.songe.gkd.data.ExcludeData
import li.songe.gkd.data.RawSubscription
import li.songe.gkd.data.SubsConfig
import li.songe.gkd.db.DbSet
import li.songe.gkd.ui.component.AppNameText
import li.songe.gkd.ui.component.AppPageScaffold
import li.songe.gkd.ui.component.EmptyText
import li.songe.gkd.ui.component.FixedTimeText
import li.songe.gkd.ui.component.GroupNameText
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.TextListDialog
import li.songe.gkd.ui.component.useListScrollState
import li.songe.gkd.ui.component.waitResult
import li.songe.gkd.ui.share.ListPlaceholder
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.ui.style.iconTextSize
import li.songe.gkd.ui.style.scaffoldPadding
import li.songe.gkd.util.appInfoMapFlow
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.mapState
import li.songe.gkd.util.subsMapFlow
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toast
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Serializable
data class ActionLogRoute(
    val subsId: Long? = null,
    val appId: String? = null,
) : NavKey

@Composable
fun ActionLogPage(route: ActionLogRoute) {
    val mainVm = LocalMainViewModel.current
    val vm = viewModel { ActionLogVm(route) }
    val logCount by vm.logCountFlow.collectAsState()
    val list = vm.pagingDataFlow.collectAsLazyPagingItems()
    val listState = useListScrollState(list.itemCount > 0)
    val subsMap by subsMapFlow.collectAsState()
    val appInfoMap by appInfoMapFlow.collectAsState()
    val subsId = route.subsId
    val appId = route.appId

    AppPageScaffold(
        title = "触发记录",
        // 列表进场时 content blur 会与 NavDisplay 转场同帧采样，易出 UI 线程尖峰
        enableContentBlur = false,
        navigationIcon = {
            PerfIconButton(
                imageVector = PerfIcon.ArrowBack,
                onClick = { mainVm.popPage() },
            )
        },
        actions = {
            if (logCount > 0) {
                PerfIconButton(
                    imageVector = PerfIcon.Delete,
                    onClick = throttle(fn = mainVm.viewModelScope.launchAsFn {
                        val text = when {
                            subsId != null -> "确定删除当前订阅所有触发记录?"
                            appId != null -> "确定删除当前应用所有触发记录?"
                            else -> "确定删除所有触发记录?"
                        }
                        mainVm.dialogFlow.waitResult(
                            title = "删除记录",
                            text = text,
                            error = true,
                        )
                        when {
                            subsId != null -> DbSet.actionLogDao.deleteSubsAll(subsId)
                            appId != null -> DbSet.actionLogDao.deleteAppAll(appId)
                            else -> DbSet.actionLogDao.deleteAll()
                        }
                        toast("删除成功")
                    }),
                )
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.scaffoldPadding(contentPadding),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                count = list.itemCount,
                key = list.itemKey { c -> c.first.id },
            ) { i ->
                val item = list[i] ?: return@items
                val lastItem = if (i > 0) list[i - 1] else null
                ActionLogCard(
                    item = item,
                    lastItem = lastItem,
                    onClick = { vm.showActionLogFlow.value = item.first },
                    subsId = subsId,
                    appId = appId,
                    subscriptionName = subsMap[item.first.subsId]?.name,
                    appInfo = appInfoMap[item.first.appId],
                )
            }
            item(ListPlaceholder.KEY, ListPlaceholder.TYPE) {
                Spacer(modifier = Modifier.height(EmptyHeight))
                if (logCount == 0 && list.loadState.refresh !is LoadState.Loading) {
                    EmptyText(text = "暂无数据")
                }
            }
        }

        vm.showActionLogFlow.collectAsState().value?.let {
            ActionLogDialog(
                vm = vm,
                actionLog = it,
                onDismissRequest = { vm.showActionLogFlow.value = null },
            )
        }
    }
}

@Composable
private fun ActionLogCard(
    modifier: Modifier = Modifier,
    item: Triple<ActionLog, RawSubscription.RawGroupProps?, RawSubscription.RawRuleProps?>,
    lastItem: Triple<ActionLog, RawSubscription.RawGroupProps?, RawSubscription.RawRuleProps?>?,
    onClick: () -> Unit,
    subsId: Long?,
    appId: String?,
    subscriptionName: String?,
    appInfo: AppInfo?,
) {
    val mainVm = LocalMainViewModel.current
    val (actionLog, group, rule) = item
    val lastActionLog = lastItem?.first
    val isDiffApp = actionLog.appId != lastActionLog?.appId
    val colorScheme = MiuixTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        if (isDiffApp && appId == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (lastItem == null) 0.dp else 4.dp)
                    .squircleClip(cornerRadius = 8.dp)
                    .clickable(onClick = throttle {
                        mainVm.navigatePage(AppConfigRoute(appId = actionLog.appId))
                    })
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppNameText(
                    appId = actionLog.appId,
                    appInfo = appInfo,
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.subtitle,
                    color = colorScheme.primary,
                )
                PerfIcon(
                    imageVector = PerfIcon.KeyboardArrowRight,
                    modifier = Modifier.iconTextSize(),
                    tint = colorScheme.primary,
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
            onClick = onClick,
            showIndication = true,
        ) {
            FixedTimeText(
                text = actionLog.date,
                style = MiuixTheme.textStyles.footnote1,
                color = colorScheme.onSurfaceVariantSummary,
            )
            val showActivityId = actionLog.showActivityId
            if (showActivityId != null) {
                Text(
                    text = showActivityId,
                    style = MiuixTheme.textStyles.body2,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Text(
                    text = "null",
                    style = MiuixTheme.textStyles.body2,
                    color = colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (subsId == null) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = subscriptionName ?: "id=${actionLog.subsId}",
                        style = MiuixTheme.textStyles.body2,
                    )
                    Text(
                        text = "v${item.first.subsVersion}",
                        style = MiuixTheme.textStyles.footnote2,
                        color = colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colorScheme.primaryContainer)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val groupDesc = group?.name.toString()
                GroupNameText(
                    isGlobal = actionLog.groupType == SubsConfig.GlobalGroupType,
                    text = groupDesc,
                    style = MiuixTheme.textStyles.body2,
                    color = if (group?.name == null) {
                        colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        colorScheme.onSurface
                    },
                )
                val ruleDesc = rule?.name ?: (if ((group?.rules?.size ?: 0) > 1) {
                    val keyDesc = actionLog.ruleKey?.let { "key=$it, " } ?: ""
                    "${keyDesc}index=${actionLog.ruleIndex}"
                } else {
                    null
                })
                if (ruleDesc != null) {
                    Text(
                        text = ruleDesc,
                        style = MiuixTheme.textStyles.body2,
                        modifier = Modifier.padding(start = 8.dp),
                        color = colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionLogDialog(
    vm: ViewModel,
    actionLog: ActionLog,
    onDismissRequest: () -> Unit,
) {
    val mainVm = LocalMainViewModel.current
    val scope = rememberCoroutineScope()
    val subsConfig = remember(actionLog) {
        (if (actionLog.groupType == SubsConfig.AppGroupType) {
            DbSet.subsConfigDao.queryAppGroupTypeConfig(
                actionLog.subsId, actionLog.appId, actionLog.groupKey
            )
        } else {
            DbSet.subsConfigDao.queryGlobalGroupTypeConfig(actionLog.subsId, actionLog.groupKey)
        }).stateIn(vm.viewModelScope, SharingStarted.Eagerly, null)
    }.collectAsState().value

    val oldExclude = remember(subsConfig?.exclude) {
        ExcludeData.parse(subsConfig?.exclude)
    }

    val subs = remember(actionLog.subsId) {
        subsMapFlow.mapState(scope) { it[actionLog.subsId] }
    }.collectAsState().value
    val group = subs?.globalGroups?.find { g -> g.key == actionLog.groupKey }
    val appChecked = if (
        actionLog.groupType == SubsConfig.GlobalGroupType && group != null
    ) {
        getGlobalGroupChecked(
            subs,
            oldExclude,
            group,
            actionLog.appId,
        )
    } else {
        null
    }
    val activityDisabled = actionLog.activityId?.let {
        oldExclude.activityIds.contains(actionLog.appId to it)
    }

    TextListDialog(
        onDismiss = onDismissRequest,
        title = "操作",
        textList = buildList {
            add("查看规则" to {
                if (actionLog.groupType == SubsConfig.AppGroupType) {
                    mainVm.navigatePage(
                        SubsAppGroupListRoute(
                            actionLog.subsId, actionLog.appId, actionLog.groupKey
                        )
                    )
                } else if (actionLog.groupType == SubsConfig.GlobalGroupType) {
                    mainVm.navigatePage(
                        SubsGlobalGroupListRoute(
                            actionLog.subsId, actionLog.groupKey
                        )
                    )
                }
            })
            if (appChecked != null) {
                add(
                    (if (appChecked) "在此应用禁用" else "移除在此应用的禁用") to vm.viewModelScope.launchAsFn {
                        val nextConfig = subsConfig ?: SubsConfig(
                            type = SubsConfig.GlobalGroupType,
                            subsId = actionLog.subsId,
                            groupKey = actionLog.groupKey,
                        )
                        val newSubsConfig = nextConfig.copy(
                            exclude = oldExclude
                                .copy(
                                    appIds = oldExclude.appIds
                                        .toMutableMap()
                                        .apply {
                                            set(actionLog.appId, appChecked)
                                        })
                                .stringify()
                        )
                        DbSet.subsConfigDao.insert(newSubsConfig)
                        toast("更新成功")
                    }
                )
            }
            if (actionLog.activityId != null && activityDisabled != null) {
                add(
                    (if (activityDisabled) "移除在此页面的禁用" else "在此页面禁用") to vm.viewModelScope.launchAsFn {
                        val nextConfig = if (actionLog.groupType == SubsConfig.AppGroupType) {
                            subsConfig ?: SubsConfig(
                                type = SubsConfig.AppGroupType,
                                subsId = actionLog.subsId,
                                appId = actionLog.appId,
                                groupKey = actionLog.groupKey,
                            )
                        } else {
                            subsConfig ?: SubsConfig(
                                type = SubsConfig.GlobalGroupType,
                                subsId = actionLog.subsId,
                                groupKey = actionLog.groupKey,
                            )
                        }
                        val newSubsConfig = nextConfig.copy(
                            exclude = oldExclude
                                .switch(
                                    actionLog.appId,
                                    actionLog.activityId
                                )
                                .stringify()
                        )
                        DbSet.subsConfigDao.insert(newSubsConfig)
                        toast("更新成功")
                    }
                )
            }
        },
    )
}
