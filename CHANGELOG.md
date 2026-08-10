<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Leetcode Editor Changelog
[![English Document][badge:en-doc]][gh:en-doc]
[![中文文档][badge:zh-doc]][gh:zh-doc]
[![捐赠][badge:donate]][shuzijun-donate]
[![捐赠][badge:donate-zh]][shuzijun-donate]
[![内推][badge:referrals]][shuzijun-referrals]

## 9.0.0

### Added
- add startup integration tests and broader regression coverage for core plugin workflows / 为插件启动和核心操作流程新增集成测试，并扩大回归测试覆盖范围
- add an optional development-tools switch for diagnostic network output and a Settings action that sends a Sentry test exception; it is disabled in normal builds and can be enabled when launching the IDE / 新增可选的开发工具开关，用于诊断网络输出和在设置页发送 Sentry 测试异常；常规构建默认关闭，可在启动 IDE 时启用

### Changed
- require IntelliJ IDEA 2026.2 (build 262) or later and upgrade the runtime baseline to Java 25 / 最低兼容版本调整为 IntelliJ IDEA 2026.2（build 262），运行和构建基线升级至 Java 25
- move network requests, question loading, preview rendering, and cache cleanup away from the IDE UI thread to reduce freezes ([#767](https://github.com/shuzijun/leetcode-editor/pull/767)) / 将网络请求、题目加载、预览渲染和缓存清理移出 IDE UI 线程，减少界面卡顿
- reorganize editor actions into a clearer View submenu and refresh action names and descriptions / 将编辑器操作重新组织到更清晰的 View 子菜单，并更新操作名称和说明
- refresh the question navigator, result console, difficulty colors, and plugin icons / 更新题目导航、运行结果控制台、默认难度颜色和插件图标
- show update information as an IDE notification with a link to the changelog / 版本升级后通过 IDE 通知展示更新提示，并提供更新日志链接
- keep informational output in the console without forcing it to the foreground; errors still activate the console / 普通信息输出不再强制激活控制台，错误信息仍会主动显示控制台
- render question previews through Vditor's lightweight read-only API, with delayed resource loading and explicit readiness reporting for a faster, more reliable preview / 通过 Vditor 轻量只读 API 渲染题目预览，并延迟加载资源和显式上报就绪状态，提升预览速度与稳定性
- update documentation and release workflows for the new platform baseline / 更新项目文档和发布流程以适配新的平台基线
- migrate shared LeetCode transports to the lc-sdk 0.0.4-rc.4 typed APIs, consolidating request models and default implementations / 将共享力扣请求迁移到 lc-sdk 0.0.4-rc.4 类型化 API，并统一请求模型和默认实现
- upgrade Sentry to 8.x and update error reporting to its supported API / 将 Sentry 升级至 8.x，并迁移错误上报至受支持的 API

### Fixed
- fix question pagination, filtering, table refreshes, and stale asynchronous navigator results / 修复题目分页、筛选、列表刷新以及异步请求结果过期导致的导航异常
- fix favorites by consistently using frontend question IDs / 统一使用前端题目 ID，修复收藏状态异常
- fix editor and JCEF preview initialization on IntelliJ IDEA 2026.2 / 修复 IntelliJ IDEA 2026.2 中编辑器和 JCEF 预览初始化失败的问题
- migrate HTTP proxy integration to the IntelliJ IDEA 2026.2 proxy APIs / 将 HTTP 代理集成迁移至 IntelliJ IDEA 2026.2 的代理 API
- fix plugin description metadata shown by the IDE / 修复 IDE 插件详情页中的插件描述元数据
- fix stale account email-verification warnings after login / 修复登录后仍显示过期邮箱验证警告的问题
- suppress expected JCEF login redirect load-error notifications / 屏蔽 JCEF 登录重定向过程中预期发生的页面加载错误提示
- fix regressions introduced by the asynchronous UI work, including preview loading, project state cleanup, and action registration / 修复异步 UI 改造引入的回归问题，包括预览加载、项目状态清理和操作注册异常

## 8.16.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#760](https://github.com/shuzijun/leetcode-editor/issues/760)
### Removed


## 8.15.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#756](https://github.com/shuzijun/leetcode-editor/issues/756)
### Removed



## 8.14.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#726](https://github.com/shuzijun/leetcode-editor/issues/726)
-
### Removed


## 8.13.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#719](https://github.com/shuzijun/leetcode-editor/issues/719)
- 
### Removed


## 8.12.0

### Added
- Code Type: Pandas & PostgreSQL enhancement [#709](https://github.com/shuzijun/leetcode-editor/issues/709)
- 希望点击搜索按钮时可以自动聚焦到搜索框 [#713](https://github.com/shuzijun/leetcode-editor/issues/713)
### Changed

### Deprecated

### Fixed
- fix [#708](https://github.com/shuzijun/leetcode-editor/issues/708)
- fix [#712](https://github.com/shuzijun/leetcode-editor/issues/712)
- 
### Removed


## 8.11.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#701](https://github.com/shuzijun/leetcode-editor/issues/701)

### Removed



## 8.10.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#697](https://github.com/shuzijun/leetcode-editor/issues/697)

### Removed


## 8.9.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#682](https://github.com/shuzijun/leetcode-editor/issues/682)

### Removed


## 8.8.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#636](https://github.com/shuzijun/leetcode-editor/issues/636)
- fix [#638](https://github.com/shuzijun/leetcode-editor/issues/638)

### Removed


## 8.7.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#613](https://github.com/shuzijun/leetcode-editor/issues/613)
- fix [#625](https://github.com/shuzijun/leetcode-editor/issues/625)

### Removed

## 8.6.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#601](https://github.com/shuzijun/leetcode-editor/issues/601)

### Removed

## 8.5.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#598](https://github.com/shuzijun/leetcode-editor/issues/598)

### Removed

## 8.4.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#566](https://github.com/shuzijun/leetcode-editor/issues/566)
- fix [#567](https://github.com/shuzijun/leetcode-editor/issues/567)

### Removed


## 8.3.0

### Added

### Changed

### Deprecated

### Fixed
- fix [#545](https://github.com/shuzijun/leetcode-editor/issues/545)
- fix [#538](https://github.com/shuzijun/leetcode-editor/issues/538)

### Removed


## 8.2.0

### Added
- 增加了不同的窗口,包括*分页窗口*、*全部题目窗口*、*[CodeTop](https://codetop.cc/?utm_source=leetcode_editor)窗口*,可以在导航栏中通过按钮切换.
- Added different windows, including paging window, all problem window, [CodeTop](https://codetop.cc/?utm_source=leetcode_editor) window, which can be switched by buttons in the navigation bar.

- 增加数据统计信息存储,可配合[action](https://github.com/shuzijun/leetcode-editor/blob/master/action/README_ZH.md)生成勋章
- Increase the storage of data statistics, you can use [action](https://github.com/shuzijun/leetcode-editor/tree/master/action) to generate medals


### Changed
- 修改消息通知方式
- Modify the message notification method

- 更改窗口位置
- Change window position
### Deprecated

### Fixed
- fix bugs

### Removed

## 0.0.0  

### Added   
    
### Changed    
- Snapshot

### Deprecated

### Removed

[badge:en-doc]: https://img.shields.io/badge/Docs-English%20Document-blue?logo=docs&style=flat-square
[badge:zh-doc]: https://img.shields.io/badge/Docs-中文文档-blue?logo=docs&style=flat-square
[badge:donate]: https://img.shields.io/badge/Docs-donate-ff69c4?logo=docs&style=flat-square
[badge:donate-zh]: https://img.shields.io/badge/Docs-捐赠-ff69c4?logo=docs&style=flat-square
[badge:referrals]: https://img.shields.io/badge/Docs-内推-ff69c4?logo=docs&style=flat-square


[gh:en-doc]: https://github.com/shuzijun/leetcode-editor/blob/master/README.md
[gh:zh-doc]: https://github.com/shuzijun/leetcode-editor/blob/master/README_ZH.md

[shuzijun-donate]: https://shuzijun.cn/donate.html
[shuzijun-referrals]: https://shuzijun.cn/referrals.html
