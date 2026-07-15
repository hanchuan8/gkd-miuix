# GKD-X (gkd-miuix)

基于 [GKD](https://github.com/gkd-kit/gkd) 的 Android 自定义屏幕点击应用分支，界面全面适配 [compose-miuix-ui](https://github.com/compose-miuix-ui/miuix)。

通过自定义规则，在指定界面满足条件（如屏幕存在特定文字）时，点击节点、位置或执行其他操作。

- **应用品牌**：GKD-X（`applicationId`: `li.songe.gkdx`）
- **界面**：MIUIX（顶栏 / 底栏 / 设置分组 / 对话框 / 图标等）
- **能力**：与上游 GKD 相同的选择器、订阅规则、快照与自动化能力

## 免责声明

**本项目遵循 [GPL-3.0](/LICENSE) 开源，仅供学习交流，禁止用于商业或非法用途。**

上游 GKD 项目声明同样适用，请遵守当地法律法规。

## 与上游的关系

| 项目 | 说明 |
| ---- | ---- |
| 上游 | [gkd-kit/gkd](https://github.com/gkd-kit/gkd) |
| 本仓库 | [hanchuan8/gkd-miuix](https://github.com/hanchuan8/gkd-miuix) |
| 文档 / 选择器说明 | 仍可参考 <https://gkd.li> |
| 订阅规则 | 兼容 GKD 订阅格式，可使用社区订阅 |

## 安装

从本仓库 [Releases](https://github.com/hanchuan8/gkd-miuix/releases) 下载安装包。

也可自行编译：

```bash
./gradlew :app:assembleGkdRelease
```

Debug 包：

```bash
./gradlew :app:assembleGkdDebug
```

如遇规则 / 选择器问题，可先查阅上游 [疑难解答](https://gkd.li/guide/faq)。

## 主要改动（相对上游）

- 全量 MIUIX 界面与图标资源
- 首页、设置等使用 Preference 分组布局
- 触发提示支持悬浮窗 / Toast / 实时通知等样式
- 订阅页改为顶栏刷新（取消下拉刷新）
- Debug 包不再追加应用名 `-debug` 后缀

## 订阅

默认不内置规则，需自行添加本地规则或通过订阅链接获取远程规则。

第三方订阅可参考：<https://github.com/topics/gkd-subscription>

## 反馈

问题与建议请提交到本仓库 Issues：

<https://github.com/hanchuan8/gkd-miuix/issues>
