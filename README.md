# [![Leetcode Editor][plugin-logo]][gh:leetcode-editor] Leetcode Editor

[![Release][badge:release]][gh:releases]
[![Snapshot][badge:snapshot]][gh:snapshot]
[![License][badge:license]][gh:license]
[![Plugin Homepage][badge:plugin-homepage]][plugin-homepage]
[![Version][badge:version]][plugin-versions]
[![Pro Plugin Homepage][badge:plugin-homepage-pro]][plugin-homepage-pro]
[![Version][badge:pro-version]][plugin-versions-pro]
[![Downloads][badge:downloads]][plugin-homepage]
[![English Document][badge:en-doc]][gh:en-doc]
[![中文文档][badge:zh-doc]][gh:zh-doc]
[![捐赠][badge:donate]][shuzijun-donate]
[![内推][badge:referrals]][shuzijun-referrals]

<p align="center"><img src="doc/leetcode-demo.svg" alt="LeetCode Editor overview" width="860"></p>

---

## Introduction

Solve LeetCode problems without leaving your IDE. LeetCode Editor supports
`leetcode.com` and `leetcode.cn` for problem browsing, local debugging, and submissions.

It is designed for JetBrains IDEs, including **IntelliJ IDEA**, **PhpStorm**,
**WebStorm**, **PyCharm**, **RubyMine**, **AppCode**, **CLion**, **GoLand**,
**DataGrip**, **Rider**, **MPS**, and **Android Studio**.

- [English documentation][gh:en-doc]
- [中文文档][gh:zh-doc]
- [Login Help][gh:login-help]
- [Custom Code][gh:custom-code] ([example][gh:leetcode-question])
- More open functionality: [shuzijun/lc-sdk](https://github.com/shuzijun/lc-sdk)
  
## Getting Started  
<p align="center"><img src="doc/leetcode-editor-3.0.svg" alt="Browse, code, run, and submit workflow" width="860"></p>
 

## Local debugging  
<p align="center"><img src="doc/customConfig-100.svg" alt="Custom template and local debugging workflow" width="860"></p>
  

### Installation ([help][managing-plugins])

- Install from the [JetBrains Marketplace][plugin-homepage].
- Download a package from [GitHub Releases][gh:releases].
- Support the project with [LeetCode Editor Pro][plugin-homepage-pro].

### Configuration

<p align="center"><img src="doc/config-3.0.svg" alt="LeetCode Editor settings" width="860"></p>

- **Path:** `File` → `Settings` → `Tools` → `LeetCode Plugin`
- **`URL options`:** Choose `leetcode.com` or `leetcode.cn`.
- **`Code Type`:** Java, Python, C++, Python 3, C, C#, JavaScript, Ruby, Swift, Go, Scala, Kotlin, Rust, PHP, Bash, or SQL.
- **`LoginName` / `Password`:** Credentials for the selected site.
- **`Temp File Path`:** Directory for generated temporary files.
- **`proxy (HTTP Proxy)`:** Configure at `File` → `Settings` → `Appearance & Behavior` → `System Settings` → `HTTP Proxy`.
- **`Custom code template`:** Customize generated code ([details][gh:custom-code], [example][gh:leetcode-question]).
- **`LevelColour`:** Customize problem-difficulty colors; restart the IDE after changing it.

### Problem Navigator

<p align="center"><img src="doc/window-3.0.svg" alt="LeetCode Editor problem navigator" width="860"></p>

- **Toolbar**
  - ![login][icon:login] **Sign in:** Configure an account for the selected website. Accounts for the two sites are independent.
  - ![logout][icon:logout] **Sign out:** Exit the current account. If sign-in fails, try signing out first.
  - ![refresh][icon:refresh] **Refresh:** Load problems without signing in; submitting still requires an account.
  - ![pick][icon:pick] **Pick:** Open a random problem.
  - ![find][icon:find] **Find:** Open the panel for searching, filtering, and sorting.
  - ![progress][icon:progress] **Session:** View or switch sessions.
  - ![toggle][icon:toggle] **Toggle List:** Switch between All Problem List, Paginated Problem List, and CodeTop Problem List.
  - ![config][icon:config] **Settings:** Open the configuration page.
  - ![clear][icon:clear] **Clear:** Remove files from the cache directory for the current website. Use it carefully when work has not been submitted.

### Context Menu
<p align="center"><img src="doc/menu-3.0.svg" alt="LeetCode Editor action menu" width="860"></p>

- **Problem menu:** Right-click a problem to use the following actions:
  - **Open question:** Open the problem; double-clicking a problem does the same.
  - **Open content:** Show the description and images (requires Markdown support).
  - **Open solution:** Show the official solution.
  - **Open in web:** Open the problem in a browser.
  - **Submit:** Submit the current solution.
  - **Submissions:** View submission records and select **Show detail** in the pop-up window.
  - **Run Code:** Run the default test case for the problem.
  - **Testcase:** Create custom test cases.
  - **Favorite:** Add or remove the problem from favorites.
  - **Note:** Open notes for the problem.
  - **Timer:** Track solving time in the status bar.
  - **Clear cache:** Remove cached files for the current problem.
- **Editor menu:** Right-click in the editor to access the same actions.
- **Question editor tabs:** **Content** shows the description and images, **Solution** shows the solution, **Submissions** shows submission records, and **Note** shows notes.

### Support

- [Donate][shuzijun-donate]


[plugin-logo]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/META-INF/pluginIcon.svg

[badge:plugin-homepage]: https://img.shields.io/badge/Plugin%20Home-Leetcode%20Editor-blue?logo=jetbrains&style=flat-square
[badge:plugin-homepage-pro]: https://img.shields.io/badge/Pro%20Plugin%20Home-Leetcode%20Editor%20Pro-blue?logo=jetbrains&style=flat-square&color=blueviolet
[badge:release]: https://img.shields.io/github/actions/workflow/status/shuzijun/leetcode-editor/release.yml?branch=master&style=flat-square&logo=github&&label=Release%20Build
[badge:snapshot]: https://img.shields.io/github/actions/workflow/status/shuzijun/leetcode-editor/snapshot.yml?branch=master&style=flat-square&logo=github&&label=Snapshot%20Build
[badge:license]: https://img.shields.io/github/license/shuzijun/leetcode-editor.svg?style=flat-square&&label=License
[badge:downloads]: https://img.shields.io/jetbrains/plugin/d/12132?style=flat-square&label=Plugin%20Downloads&logo=jetbrains
[badge:version]: https://img.shields.io/jetbrains/plugin/v/12132?label=Plugin%20Version&logo=jetbrains&style=flat-square
[badge:pro-version]: https://img.shields.io/jetbrains/plugin/v/17166?label=Pro%20Plugin%20Version&logo=jetbrains&style=flat-square&color=blueviolet
[badge:en-doc]: https://img.shields.io/badge/Docs-English%20Document-blue?logo=docs&style=flat-square
[badge:zh-doc]: https://img.shields.io/badge/Docs-中文文档-blue?logo=docs&style=flat-square
[badge:donate]: https://img.shields.io/badge/Docs-donate-ff69c4?logo=docs&style=flat-square
[badge:referrals]: https://img.shields.io/badge/Docs-referrals-ff69c4?logo=docs&style=flat-square

[icon:leetcode]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/LeetCode_dark.svg
[icon:login]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/login_dark.svg
[icon:logout]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/logout_dark.svg
[icon:refresh]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/refresh_dark.svg
[icon:pick]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/random_dark.svg
[icon:find]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/find_dark.svg
[icon:progress]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/progress_dark.svg
[icon:toggle]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/toggle_dark.svg
[icon:config]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/config_lc_dark.svg
[icon:clear]: https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/src/main/resources/icons/clear_dark.svg

[gh:leetcode-editor]: https://github.com/shuzijun/leetcode-editor
[gh:releases]: https://github.com/shuzijun/leetcode-editor/releases
[gh:snapshot]: https://github.com/shuzijun/leetcode-editor/actions?query=workflow%3ASnapshot
[gh:license]: https://github.com/shuzijun/leetcode-editor/blob/master/LICENSE
[gh:en-doc]: #Introduction
[gh:zh-doc]: https://github.com/shuzijun/leetcode-editor/blob/master/README_ZH.md
[gh:login-help]: https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp.md
[gh:custom-code]: https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode.md
[gh:leetcode-question]: https://github.com/shuzijun/leetcode-question
[gh:question]: https://github.com/shuzijun/leetcode-editor/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98

[plugin-homepage]: https://plugins.jetbrains.com/plugin/12132-leetcode-editor
[plugin-versions]: https://plugins.jetbrains.com/plugin/12132-leetcode-editor/versions
[plugin-homepage-pro]: https://plugins.jetbrains.com/plugin/17166-leetcode-editor-pro
[plugin-versions-pro]: https://plugins.jetbrains.com/plugin/17166-leetcode-editor-pro/versions
[managing-plugins]: https://www.jetbrains.com/help/idea/managing-plugins.html

[shuzijun-donate]: https://shuzijun.cn/donate.html
[shuzijun-referrals]: https://shuzijun.cn/referrals.html
