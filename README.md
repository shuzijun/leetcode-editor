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

<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/blog-comment@v0.0.1/doc/leetcode-demo.gif" alt="demo" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>

---

## Introduction  
  Do Leetcode exercises in IDE, support `leetcode.com` and `leetcode.cn`, meet the needs of problem solving and debugging.
  Support theoretically: **IntelliJ IDEA**,**PhpStorm**,**WebStorm**,**PyCharm**,**RubyMine**,**AppCode**,**CLion**,**GoLand**,**DataGrip**,**Rider MPS**,**Android Studio**.  

  - [English Document][gh:en-doc]
  - [中文文档][gh:zh-doc]

    - Useful Links
      - [Login Help][gh:login-help]
      - [Custom Code][gh:custom-code] ([demo][gh:leetcode-question])  
      
  - **More open functions**: [shuzijun/lc-sdk](https://github.com/shuzijun/lc-sdk)
  
## Getting Started  
<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/leetcode-editor-3.0.gif" alt="start" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>
 

## Local debugging  
<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/customConfig-100.gif" alt="loacl" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>
  

### Installation([help][managing-plugins])  
- **Install via plug-in library** [leetcode-editor][plugin-homepage]  
- **Install by downloading the file** [releases][gh:releases]    
- **If you are willing to donate to this project, you can choose the pro version** [leetcode-editor-pro][plugin-homepage-pro]  

### Configuration (configuration for first installation)  

<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/config-3.0.jpg" alt="config" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>

 
- **Configuration path**: `File` -> `settings`->`tools`->`leetcode plugin`  
  - **`URL options`**: `leetcode.com`OR`leetcode.cn`  
  - **`Code Type`**: `Java`,`Python`,`C++`,`Python3`,`C`,`C#`,`JavaScript`,`Ruby`,`Swift`,`Go` ,`Scala`,`Kotlin`,`Rust`,`PHP`,`Bash`,`SQL`   
  - **`LoginName`**: Login Username
  - **`Password`**: Login password  
  - **`Temp File Path`**: Temporary file storage catalogue  
  - **`proxy(HTTP Proxy)`**: HTTP Proxy,config path:`File` -> `settings`->`Appearance & Behavior`->`System Settings`->`HTTP Proxy`
  - **`Custom code template`**: Custom code template ([details][gh:custom-code]) ([demo][gh:leetcode-question])  
  - **`LevelColour`**: Customize the difficulty color of the question, it will take effect after restart
  
### Window    

<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/window-3.0.jpg" alt="window" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>  
  
- **Toolbar**:  
  - ![login][icon:login]**`Sign in`**:The login accounts of the two websites are not interoperable and the corresponding users need to be configured when switching websites.  
  - ![logout][icon:logout]**`Sign out`**:Exit the current account, if you encounter a login error, try to exit first.  
  - ![refresh][icon:refresh]**`Refresh`**:You can also refresh and load questions if you are not logged in, but you cannot submit it.  
  - ![pick][icon:pick]**`pick`**:Open a problem randomly.  
  - ![find][icon:find]**`Find`**:Open filter panel. You can search, filter and sort.   
  - ![progress][icon:progress]**`Session`**:Open the Session panel. You can view or switch sessions.  
  - ![toggle][icon:toggle]**`Toggle List`**:Switch to other list windows, including "All Problem List" , "Paginated Problem List" , "CodeTop Problem List".  
  - ![config][icon:config]**`Settings`**:Quick jump to the configuration page.  
  - ![clear][icon:clear]**`Clear`**:Clean up the files in the configured cache directories. The cache directories of the two websites are different and only the current configured websites are cleaned up. Carefully clean up cases without submitting.  

### Menu  
<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/menu-3.0.jpg" alt="menu" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>


- **Menu (right-click on the questions)**:  
  - **`open question`**:Open the question or double click on the question  
  - **`open content`**:Show content(Rely on Markdown)  
  - **`Submit`**:Submit the question  
  - **`Submissions`**:View the submission record, select the record details in the pop-up window(`Show detail`)  
  - **`Run Code`**:Run the code, the test case for the question is used by default  
  - **`Testcase`**:Customize test cases  
  - **`favorite`**:Add or remove favorite
  - **`Clear cache`**:Clean up the current question  
  - **`Timer`**:Timer, when it is turned on, it will prompt the problem solving time in the status bar in the lower right corner    

<br> 

- **Editor Menu(Right-click on Editor to appear)**:  
  Function as above  

<br>  

- **Question Editor**:
  - **`Content`**:Show content(Rely on Markdown)
  - **`Solution`**:show solution
  - **`Submissions`**:View the submission record
  - **`Note`**:Show note 

### Support and Donations
* [donate][shuzijun-donate]


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