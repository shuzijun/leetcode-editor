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

<p align="center"><img src="https://s1.imagehub.cc/images/2022/07/10/leetcode-demo.gif" alt="demo" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>

---

## 简介  
  在IDE中解决LeetCode问题,支持`leetcode.com`与`leetcode.cn`,满足做题和调试的需求。  
  理论上支持: **IntelliJ IDEA**,**PhpStorm**,**WebStorm**,**PyCharm**,**RubyMine**,**AppCode**,**CLion**,**GoLand**,**DataGrip**,**Rider MPS**,**Android Studio**。  

  - [English Document][gh:en-doc]
  - [中文文档][gh:zh-doc]

  - 有用的链接
    - [登录帮助][gh:login-help]
    - [自定义代码生成][gh:custom-code-zh] ([示例][gh:leetcode-question])


##  开始使用 
<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/leetcode-editor-3.0.gif" alt="start" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>
 

## 本地调试  
<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/customConfig-100.gif" alt="loacl" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>


### 安装([help][managing-plugins])  
- **通过插件库安装** [leetcode-editor][plugin-homepage]  
- **下载文件安装** [releases][gh:releases]  
- **如果您想捐助此项目,可以选择Pro版本** [leetcode-editor-pro][plugin-homepage-pro]

### 配置(第一次安装需要先配置)  

<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/config-3.0.jpg" alt="config" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>

 
- **配置路径**: `File` -> `settings`->`tools`->`leetcode plugin`  
  - **`URL可选项`**: `leetcode.com`与`leetcode.cn`  
  - **`Code Type`**: `Java`,`Python`,`C++`,`Python3`,`C`,`C#`,`JavaScript`,`Ruby`,`Swift`,`Go` ,`Scala`,`Kotlin`,`Rust`,`PHP`,`Bash`,`SQL`   
  - **`LoginName`**: 登录用户名
  - **`Password`**: 登录密码  
  - **`Temp File Path`**: 临时文件存放目录  
  - **`proxy(HTTP Proxy)`**: 使用http代理,配置路径:`File` -> `settings`->`Appearance & Behavior`->`System Settings`->`HTTP Proxy`
  - **`Custom code template`**: 自定义代码生成模板 ([详细介绍][gh:custom-code-zh])([示例][gh:leetcode-question])  
  - **`LevelColour`**: 自定义题目难度颜色,重启后生效  
  - **`English Content`**: 题目显示英文描述  
  
### 窗口    

<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/window-3.0.jpg" alt="window" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p> 
  
- **工具栏**:  
  - ![login][icon:login]**`登录`**:两个网站的登录帐号不互通，切换网站需配置对应的用户  
  - ![logout][icon:logout]**`退出`**:退出当前账户,如遇到登录错误,尝试先进行退出  
  - ![refresh][icon:refresh]**`刷新`**:在未登录的情况下也可查看刷新加载题目，但是无法提交  
  - ![pick][icon:pick]**`随机`**:随机一个题目
  - ![find][icon:find]**`查找`**:打开筛选面板,可以进行搜索、过滤或者排序.  
  - ![progress][icon:progress]**`会话`**:打开进度面板,可以查看或者切换进度.
  - ![toggle][icon:toggle]**`切换列表`**:切换到其他列表窗口,包括"所有题目列表"、"分页题目列表"、"CodeTop题目列表".
  - ![config][icon:config]**`配置`**:快捷跳转到配置界面  
  - ![clear][icon:clear]**`清理`**:清理配置的缓存目录下的文件，两个网站对应的缓存目录不同，只会清理当前配置的网站下的。部分题目未提交的情况下慎重清理  

### 菜单  
<p align="center"><img src="https://cdn.jsdelivr.net/gh/shuzijun/leetcode-editor@master/doc/menu-3.0.jpg" alt="menu" style="width: auto;height: auto;max-width: 90%; max-height: 90%;"></p>
   

- **菜单(在题目上右击出现)**:  
  - **`open question`**:打开题目,在题目上双击也可以打开  
  - **`open content`**:查看描述，包含图片(依赖 Markdown)  
  - **`Submit`**:提交题目  
  - **`Submissions`**:查看提交记录,在弹出的窗口上选择记录查看详情(`Show detail`)  
  - **`Run Code`**:运行代码,默认使用题目的测试用例  
  - **`Testcase`**:自定义测试用例  
  - **`favorite`**:添加或移除收藏
  - **`Clear cache`**:清理当前题目  
  - **`Timer`**:计时器,开启后在右下角状态栏提示解题时间    

<br>  

- **Editor菜单(在Editor上右击出现)**:  
  功能同上  

<br>  

- **问题编辑器**:
  - **`Content`**:查看描述，包含图片
  - **`Solution`**:查看解答
  - **`Submissions`**:查看提交记录
  - **`Note`**:查看笔记
  
### 常见问题  
  [常见问题][gh:question] 

### 支持和捐赠
* [捐赠][shuzijun-donate]
* [内推][shuzijun-referrals]


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
[badge:donate]: https://img.shields.io/badge/Docs-捐赠-ff69c4?logo=docs&style=flat-square
[badge:referrals]: https://img.shields.io/badge/Docs-内推-ff69c4?logo=docs&style=flat-square


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
[gh:en-doc]: https://github.com/shuzijun/leetcode-editor/blob/master/README.md
[gh:zh-doc]: #简介
[gh:login-help]: https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp_ZH.md
[gh:custom-code-zh]: https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode_ZH.md
[gh:leetcode-question]: https://github.com/shuzijun/leetcode-question
[gh:question]: https://github.com/shuzijun/leetcode-editor/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98

[plugin-homepage]: https://plugins.jetbrains.com/plugin/12132-leetcode-editor
[plugin-versions]: https://plugins.jetbrains.com/plugin/12132-leetcode-editor/versions
[plugin-homepage-pro]: https://plugins.jetbrains.com/plugin/17166-leetcode-editor-pro
[plugin-versions-pro]: https://plugins.jetbrains.com/plugin/17166-leetcode-editor-pro/versions
[managing-plugins]: https://www.jetbrains.com/help/idea/managing-plugins.html

[shuzijun-donate]: https://shuzijun.cn/donate.html
[shuzijun-referrals]: https://shuzijun.cn/referrals.html
