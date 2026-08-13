<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Leetcode Editor Pro Changelog

[![English Document][badge:en-doc]][gh:en-doc]
[![中文文档][badge:zh-doc]][gh:zh-doc]
[![捐赠][badge:donate]][shuzijun-donate]
[![捐赠][badge:donate-zh]][shuzijun-donate]
[![内推][badge:referrals]][shuzijun-referrals]

## Unreleased

## 2026.2.0

### Added

- add a redesigned Pro result console with structured run and submission entries / 新增重新设计的 Pro 结果控制台，以结构化方式展示运行和提交结果
- add side-by-side Expected and Actual output comparison, with a detailed comparison view for failed cases / 新增 Expected 与 Actual 并排对比，并可在失败用例中查看完整差异
- show complete compiler diagnostics for failed code execution and provide direct code-location links where available / 运行编译失败时展示完整诊断信息，并在可用时提供代码位置跳转链接

### Changed

- require IntelliJ IDEA 2026.2 (build 262) or later and upgrade the runtime baseline to Java 25 / 最低兼容版本调整为 IntelliJ IDEA 2026.2（build 262），运行和构建基线升级至 Java 25
- move network requests, question loading, preview rendering, and cache cleanup away from the IDE UI thread to reduce freezes ([#767](https://github.com/shuzijun/leetcode-editor/pull/767)) / 将网络请求、题目加载、预览渲染和缓存清理移出 IDE UI 线程，减少界面卡顿
- reorganize editor actions into a clearer View submenu and refresh action names and descriptions / 将编辑器操作重新组织到更清晰的 View 子菜单，并更新操作名称和说明
- refresh the question navigator, difficulty colors, and plugin icons / 更新题目导航、默认难度颜色和插件图标
- show update information as an IDE notification with a link to the changelog / 版本升级后通过 IDE 通知展示更新提示，并提供更新日志链接
- automatically open and scroll the result console to the latest run, submission, and error message / 运行、提交或发生错误时自动打开结果控制台并滚动到最新消息

### Fixed

- prevent an error dialog when a submission failure omits optional result fields, and show the failure in the console instead / 修复提交失败结果缺少可选字段时弹出异常的问题，改为在控制台正常展示失败信息
- show the complete compiler diagnostic returned by Run Code instead of only the summary message / 修复 Run Code 仅显示编译错误摘要的问题，改为展示完整编译诊断信息

## 2024.1.8

### Fixed

- fix submission failed.

## 2024.1.7

### Fixed

- fix [#760](https://github.com/shuzijun/leetcode-editor/issues/760)

## 2024.1.6

### Fixed

- fix [#756](https://github.com/shuzijun/leetcode-editor/issues/756)

## 2024.1.5

### Fixed

- fix [#726](https://github.com/shuzijun/leetcode-editor/issues/726)

## 2024.1.4

### Fixed

- fix [#719](https://github.com/shuzijun/leetcode-editor/issues/719)

## 2024.1.3

### Added

- Code Type: Pandas & PostgreSQL enhancement [#709](https://github.com/shuzijun/leetcode-editor/issues/709)
- 希望点击搜索按钮时可以自动聚焦到搜索框 [#713](https://github.com/shuzijun/leetcode-editor/issues/713)

### Fixed

- fix [#708](https://github.com/shuzijun/leetcode-editor/issues/708)
- fix [#712](https://github.com/shuzijun/leetcode-editor/issues/712)

## 2024.1.2

### Fixed

- fix [#701](https://github.com/shuzijun/leetcode-editor/issues/701)

## 2024.1.1

### Fixed

- fix [#697](https://github.com/shuzijun/leetcode-editor/issues/697)

## 2023.1.3

### Fixed

- fix [#682](https://github.com/shuzijun/leetcode-editor/issues/682)

## 2023.1.2

### Fixed

- fix [#636](https://github.com/shuzijun/leetcode-editor/issues/636)
- fix [#638](https://github.com/shuzijun/leetcode-editor/issues/638)

## 2023.1.1

### Changed

- 优化操作响应时间
- Optimize operation response time

## 2023.1.0

### Changed

- 优化操作响应时间
- Optimize operation response time

### Fixed

- fix [#613](https://github.com/shuzijun/leetcode-editor/issues/613)

## 2022.2.8

### Fixed

- fix [#601](https://github.com/shuzijun/leetcode-editor/issues/601)

## 2022.2.7

### Fixed

- fix [#598](https://github.com/shuzijun/leetcode-editor/issues/598)

## 2022.2.6

### Added

- 扩展协议,支持通过浏览器插件[Leetcode Editoe Extension](https://github.com/shuzijun/leetcode-editor-extension)登录
- Extension protocol, support login through the browser plugin [Leetcode Editoe Extension](https://github.com/shuzijun/leetcode-editor-extension)

### Changed

- 修改编辑器查看解答与提交的方式.
- Modify the way the editor views solution and submissions.
- 修改插件图标.
- Modify plugin icon.

## 2022.2.5

### Added

- 扩展协议,支持通过浏览器插件[Leetcode Editoe Extension](https://github.com/shuzijun/leetcode-editor-extension)打开题目
- Extension protocol, support opening questions through the browser plugin [Leetcode Editoe Extension](https://github.com/shuzijun/leetcode-editor-extension)
- 增加描述文件加载动画
- Add description file loading animation

## 2022.2.4

### Added

- 新增展示提示
- Add show hint [#issues/230](https://github.com/shuzijun/leetcode-editor/issues/230)
- 新增原来树形题目展示
- Add tree navigator [#issues/516](https://github.com/shuzijun/leetcode-editor/issues/516)
- 新增重置代码为默认代码
- Add reset to default code definition [#issues/462](https://github.com/shuzijun/leetcode-editor/issues/462)
- 新增完成每日签到任务
- Add daily check-in mission
- 新增可以同时配置多种语言模版
- Add multiple template configurations [#issues/552](https://github.com/shuzijun/leetcode-editor/issues/552)
- 新增配置笔记模版
- Add note template [#issues/542](https://github.com/shuzijun/leetcode-editor/issues/542)
- 新增支持切换题目语言类型
- Add switch question code type
- 新增快速下一题
- Add quickly switch question

### Changed

- 修改提交记录的文件为markdown
- Change the submission file type to markdown [#issues/546](https://github.com/shuzijun/leetcode-editor/issues/546)
- 新配置界面
- Change setting ui
- 新的通知样式
- Change notification style

### Fixed

- 修复中国站进度问题
- Fix session [#issues/566](https://github.com/shuzijun/leetcode-editor/issues/566)
- 修复表格宽度问题
- Fix table width [#issues/567](https://github.com/shuzijun/leetcode-editor/issues/567)
- Fix bugs

## 2022.2.3

### Fixed

- fix UnsupportedOperationException
- fix Acceptance Width
- fix bugs

## 2022.2.2

### Fixed

- fix bugs

## 2022.2.1

### Added

- 增加数据统计信息存储,可配合[action](https://github.com/shuzijun/leetcode-editor/blob/master/action/README_ZH.md)生成勋章
- Increase the storage of data statistics, you can use [action](https://github.com/shuzijun/leetcode-editor/tree/master/action) to generate medals

### Fixed

- fix bugs

## 2022.2.0

### Added

- 增加了不同的窗口,包括*分页窗口*、*全部题目窗口*、*[CodeTop](https://codetop.cc/?utm_source=leetcode_editor)窗口*,可以在导航栏中通过按钮切换.
- Added different windows, including paging window, all problem window, [CodeTop](https://codetop.cc/?utm_source=leetcode_editor) window, which can be switched by buttons in the navigation bar.

### Changed

- domain name change
- 修改域名

[shuzijun-referrals]: https://shuzijun.cn/referrals.html
[shuzijun-donate]: https://shuzijun.cn/donate.html
[gh:zh-doc]: https://github.com/shuzijun/leetcode-editor/blob/master/README_ZH.md
[gh:en-doc]: https://github.com/shuzijun/leetcode-editor/blob/master/README.md
[badge:zh-doc]: https://img.shields.io/badge/Docs-中文文档-blue?logo=docs&style=flat-square
[badge:referrals]: https://img.shields.io/badge/Docs-内推-ff69c4?logo=docs&style=flat-square
[badge:en-doc]: https://img.shields.io/badge/Docs-English%20Document-blue?logo=docs&style=flat-square
[badge:donate-zh]: https://img.shields.io/badge/Docs-捐赠-ff69c4?logo=docs&style=flat-square
[badge:donate]: https://img.shields.io/badge/Docs-donate-ff69c4?logo=docs&style=flat-square
