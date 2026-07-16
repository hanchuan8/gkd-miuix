package li.songe.gkd.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import li.songe.gkd.ui.component.PerfDropdownMenu
import li.songe.gkd.ui.component.PerfDropdownMenuItem
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import li.songe.gkd.data.ActionLog
import li.songe.gkd.data.RawSubscription
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.component.AnimatedBooleanContent
import li.songe.gkd.ui.component.AnimationFloatingActionButton
import li.songe.gkd.ui.component.AppPageScaffold
import li.songe.gkd.ui.component.BatchActionButtonGroup
import li.songe.gkd.ui.component.EmptyText
import li.songe.gkd.ui.component.MenuGroupCard
import li.songe.gkd.ui.component.MenuItemCheckbox
import li.songe.gkd.ui.component.MenuItemRadioButton
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.RuleGroupCard
import li.songe.gkd.ui.component.animateListItem
import li.songe.gkd.ui.component.toGroupState
import li.songe.gkd.ui.component.useListScrollState
import li.songe.gkd.ui.icon.BackCloseIcon
import li.songe.gkd.ui.share.ListPlaceholder
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.ui.style.iconTextSize
import li.songe.gkd.ui.style.scaffoldPadding
import li.songe.gkd.util.LOCAL_SUBS_ID
import li.songe.gkd.util.RuleSortOption
import li.songe.gkd.util.appInfoMapFlow
import li.songe.gkd.util.copyText
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.switchItem
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toJson5String

@Serializable
data class AppConfigRoute(
    val appId: String,
    val focusLog: ActionLog? = null,
) : NavKey

@Composable
fun AppConfigPage(route: AppConfigRoute) {
    val appId = route.appId
    val focusLog = route.focusLog
    val mainVm = LocalMainViewModel.current
    val vm = viewModel { AppConfigVm(route) }

    val ruleSortType by vm.ruleSortTypeFlow.collectAsState()
    val groupSize by vm.groupSizeFlow.collectAsState()
    val firstLoading by vm.firstLoadingFlow.collectAsState()
    val resetKey = rememberSaveable { mutableIntStateOf(0) }
    val listState = useListScrollState(
        resetKey,
        groupSize > 0,
        firstLoading,
    )
    if (focusLog != null && groupSize > 0) {
        LaunchedEffect(null) {
            if (vm.focusGroupFlow?.value != null) {
                val i = vm.subsPairsFlow.value.run {
                    var j = 0
                    forEach { (entry, groups) ->
                        groups.forEach {
                            if (entry.subsItem.id == focusLog.subsId && it.groupType == focusLog.groupType && it.key == focusLog.groupKey) {
                                return@run j
                            }
                            j++
                        }
                    }
                    -1
                }
                if (i >= 0) {
                    listState.scrollToItem(i)
                }
            }
        }
    }

    val isSelectedMode = vm.isSelectedModeFlow.collectAsState().value
    val selectedDataSet = vm.selectedDataSetFlow.collectAsState().value
    LaunchedEffect(key1 = isSelectedMode) {
        if (!isSelectedMode) {
            vm.selectedDataSetFlow.value = emptySet()
        }
    }
    LaunchedEffect(key1 = selectedDataSet.isEmpty()) {
        if (selectedDataSet.isEmpty()) {
            vm.isSelectedModeFlow.value = false
        }
    }
    BackHandler(isSelectedMode) {
        vm.isSelectedModeFlow.value = false
    }
    val appInfoMap by appInfoMapFlow.collectAsState()
    val pageTitle = if (isSelectedMode) {
        if (selectedDataSet.isNotEmpty()) selectedDataSet.size.toString() else " "
    } else {
        appInfoMap[appId]?.name ?: appId
    }
    AppPageScaffold(
        title = pageTitle,
        navigationIcon = {
            IconButton(onClick = throttle {
                if (isSelectedMode) {
                    vm.isSelectedModeFlow.value = false
                } else {
                    mainVm.popPage()
                }
            }) {
                BackCloseIcon(backOrClose = !isSelectedMode)
            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }
            AnimatedBooleanContent(
                targetState = isSelectedMode,
                contentAlignment = Alignment.TopEnd,
                contentTrue = {
                    Row {
                        PerfIconButton(
                            imageVector = PerfIcon.ContentCopy,
                            enabled = selectedDataSet.any { a -> a.appId != null },
                            onClick = throttle(vm.viewModelScope.launchAsFn(Dispatchers.Default) {
                                val selectGroups = mutableListOf<RawSubscription.RawAppGroup>()
                                vm.subsPairsFlow.value.forEach { (entry, groups) ->
                                    groups.forEach { g ->
                                        if (g is RawSubscription.RawAppGroup && selectedDataSet.any { v -> entry.subsItem.id == v.subsId && g.key == v.groupKey }) {
                                            selectGroups.add(g)
                                        }
                                    }
                                }
                                val a = RawSubscription.RawApp(
                                    id = appId,
                                    name = appInfoMapFlow.value[appId]?.name,
                                    groups = selectGroups,
                                )
                                copyText(toJson5String(a))
                            })
                        )
                        BatchActionButtonGroup(vm, selectedDataSet)
                        PerfIconButton(imageVector = PerfIcon.MoreVert, onClick = {
                            expanded = true
                        })
                    }
                },
                contentFalse = {
                    Row {
                        PerfIconButton(imageVector = PerfIcon.History, onClick = throttle {
                            mainVm.navigatePage(ActionLogRoute(appId = appId))
                        })
                        PerfIconButton(imageVector = PerfIcon.Sort, onClick = {
                            expanded = true
                        })
                    }
                },
            )
            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopStart)
            ) {
                key(isSelectedMode) {
                    PerfDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (isSelectedMode) {
                            PerfDropdownMenuItem(
                                text = "全选",
                                onClick = {
                                    expanded = false
                                    vm.selectAll()
                                }
                            )
                            PerfDropdownMenuItem(
                                text = "反选",
                                onClick = {
                                    expanded = false
                                    vm.invertSelect()
                                }
                            )
                        } else {
                            MenuGroupCard(inTop = true, title = "排序") {
                                val handleItem: (RuleSortOption) -> Unit = throttle { v ->
                                    storeFlow.update { s -> s.copy(appRuleSort = v.value) }
                                }
                                RuleSortOption.objects.forEach { s ->
                                    MenuItemRadioButton(
                                        text = s.label,
                                        selected = ruleSortType == s,
                                        onClick = {
                                            handleItem(s)
                                        },
                                    )
                                }
                            }
                            MenuGroupCard(title = "筛选") {
                                MenuItemCheckbox(
                                    text = "未启用",
                                    stateFlow = vm.showDisabledRuleFlow,
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimationFloatingActionButton(
                visible = !isSelectedMode,
                onClick = {
                    mainVm.navigatePage(
                        UpsertRuleGroupRoute(
                            subsId = LOCAL_SUBS_ID,
                            groupKey = null,
                            appId = appId
                        )
                    )
                },
                imageVector = PerfIcon.Add,
                contentDescription = "添加规则"
            )
        },
    ) { contentPadding ->
        val globalSubsConfigs by vm.globalSubsConfigsFlow.collectAsState()
        val categoryConfigs by vm.categoryConfigsFlow.collectAsState()
        val appSubsConfigs by vm.appSubsConfigsFlow.collectAsState()
        val subsPairs by vm.subsPairsFlow.collectAsState()
        LazyColumn(
            modifier = Modifier.scaffoldPadding(contentPadding),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            subsPairs.forEach { (entry, groups) ->
                val subsId = entry.subsItem.id
                stickyHeader(entry.subsItem.id) {
                    Row(
                        modifier = Modifier
                            .background(MiuixTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = throttle {
                                mainVm.navigatePage(
                                    SubsAppGroupListRoute(
                                        subsItemId = subsId,
                                        appId = appId,
                                    )
                                )
                            })
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = entry.subscription.name,
                            style = MiuixTheme.textStyles.subtitle,
                            color = MiuixTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                        PerfIcon(
                            imageVector = PerfIcon.KeyboardArrowRight,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.iconTextSize()
                        )
                    }
                }
                items(groups, { Triple(subsId, it.groupType, it.key) }) { group ->
                    val subsConfig = when (group) {
                        is RawSubscription.RawAppGroup -> appSubsConfigs
                        is RawSubscription.RawGlobalGroup -> globalSubsConfigs
                    }?.find { it.subsId == entry.subsItem.id && it.groupKey == group.key }
                    val category = when (group) {
                        is RawSubscription.RawAppGroup -> entry.subscription.getCategory(group.name)
                        is RawSubscription.RawGlobalGroup -> null
                    }
                    val categoryConfig = if (category != null) {
                        categoryConfigs?.find { it.subsId == subsId && it.categoryKey == category.key }
                    } else {
                        null
                    }
                    val isSelected = selectedDataSet.any {
                        it.subsId == subsId && it.groupType == group.groupType && it.groupKey == group.key
                    }
                    val onLongClick = {
                        if (groupSize > 1 && !isSelectedMode) {
                            vm.isSelectedModeFlow.value = true
                            vm.selectedDataSetFlow.value = setOf(
                                group.toGroupState(
                                    subsId = subsId,
                                    appId = appId,
                                )
                            )
                        }
                    }
                    val onSelectedChange = {
                        vm.selectedDataSetFlow.value =
                            selectedDataSet.switchItem(
                                group.toGroupState(
                                    subsId = subsId,
                                    appId = appId,
                                )
                            )
                    }
                    RuleGroupCard(
                        modifier = Modifier.animateListItem(),
                        subs = entry.subscription,
                        appId = appId,
                        group = group,
                        subsConfig = subsConfig,
                        categoryConfig = categoryConfig,
                        onLongClick = onLongClick,
                        isSelectedMode = isSelectedMode,
                        isSelected = isSelected,
                        onSelectedChange = onSelectedChange,
                        focusGroupFlow = vm.focusGroupFlow,
                    )
                }
            }
            item(ListPlaceholder.KEY, ListPlaceholder.TYPE) {
                Spacer(modifier = Modifier.height(EmptyHeight))
                if (groupSize == 0 && !firstLoading) {
                    EmptyText(text = if (vm.showDisabledRuleFlow.collectAsState().value) "暂无数据" else "暂无数据，或修改筛选")
                }
            }
        }
    }
}
