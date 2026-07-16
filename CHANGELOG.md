# 更新内容

## v1.1.0

- 触发记录、应用配置（含「最近触发」）改为独立 Activity，缓解进页转场掉帧
- 首页 HorizontalPager + 转场就绪后再预组邻 Tab
- 关于页 KernelSU 风格动态背景（BgEffect）与滚动渐变
- 触发记录列表性能：时间文本单节点、分页精简、关闭进场 content blur

## v1.0.0（首版）

- 全量 MIUIX 界面（顶栏模糊、悬浮底栏、液态玻璃 FAB）
- 预测式返回开关；动态取色默认关闭
- 订阅添加/修改/刷新时显示加载进度
- 触发提示支持流体云 / 灵动岛实时通知，并可自定义存在时间

## MIUIX

- 界面全面适配 MIUIX（顶栏、底栏、设置分组、对话框、图标等）
- 首页按服务 / 数据概览 / 快捷入口分组
- 触发记录页适配 MIUIX 样式与字体色
- 订阅页取消下拉刷新，顶栏增加刷新按钮

## 开源致谢

本分支基于 [GKD](https://github.com/gkd-kit/gkd)，界面依托 [compose-miuix-ui](https://github.com/compose-miuix-ui/miuix)（MIUIX UI / Preference / Icons / Blur）等开源项目，详见仓库 README。

## 更新方式

- GKD - 设置 - 关于 - 检测更新
- 或前往 [GitHub Releases](https://github.com/hanchuan8/gkd-miuix/releases) 下载
