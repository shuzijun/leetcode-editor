# leetcode-editor   

  - [English Document](https://github.com/shuzijun/leetcode-editor/blob/master/README.md)  
  - [中文文档](#简介)  
    
## 简介  
  在IDE中解决LeetCode问题,支持`leetcode.com`与`leetcode-cn.com`,满足基本的做题需求。  
  理论上支持: IntelliJ IDEA  PhpStorm  WebStorm  PyCharm  RubyMine  AppCode  CLion  GoLand  DataGrip  Rider MPS  Android Studio。  
  [be with you!](https://leetcode-editor.herokuapp.com/hour.html)
  
## 使用方式  
<p align="center">
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/leetcode-editor-3.0.gif" alt="demo"/>
</p>  

### 安装  
- **通过插件库安装** https://plugins.jetbrains.com/plugin/12132-leetcode-editor  
- **下载文件安装** https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/leetcode-editor.zip  

### 配置(第一次安装需要先配置)  

<p align="center">
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/config-3.0.png" alt="settings"/>
</p>  
 
- **配置路径**: `File` -> `settings`->`tools`->`leetcode plugin`  
  - **`URL可选项`**: `leetcode.com`与`leetcode-cn.com`  
  - **`Code Type`**: `Java`,`Python`,`C++`,`Python3`,`C`,`C#`,`JavaScript`,`Ruby`,`Swift`,`Go` ,`Scala`,`Kotlin`,`Rust`,`PHP`   
  - **`LoginName`**: 登录用户名
  - **`Password`**: 登录密码  
  - **`Temp File Path`**: 临时文件存放目录  
  - **`proxy(HTTP Proxy)`**: 使用http代理,配置路径:`File` -> `settings`->`Appearance & Behavior`->`System Settings`->`HTTP Proxy`
  - **`Custom code template`**: 自定义代码生成模板 ([详细介绍](https://github.com/shuzijun/leetcode-editor/blob/master/CustomCode_ZH.md))([示例](https://github.com/shuzijun/leetcode-question))
  
### 窗口(主窗口右下角![icon](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/LeetCodeIcon.png))  
  
<p align="center">
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/window-3.0.png" alt="window"/>
</p>  
  
- **工具栏**:  
  - ![login](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/login.png)**`登录`**:两个网站的登录帐号不互通，切换网站需配置对应的用户  
  - ![logout](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/logout.png)**`退出`**:退出当前账户,如遇到登录错误,尝试先进行退出  
  - ![refresh](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/refresh.png)**`刷新`**:在未登录的情况下也可查看刷新加载题目，但是无法提交  
  - ![find](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/find.png)**`查找`**:输入内容后回车搜索，再次回车搜索下一个，只会搜索题库节点下  
  - ![collapse](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/collapseAll.png)**`折叠`**:折叠全部节点.  
  - ![config](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/config.png)**`配置`**:快捷跳转到配置界面  
  - ![clear](https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/clear.png)**`清理`**:清理配置的缓存目录下的文件，两个网站对应的缓存目录不同，只会清理当前配置的网站下的。部分题目未提交的情况下慎重清理  

- **树**:  
  - **`Problems`**:全部题目  
  - **`Difficulty`**:难度分类  
  - **`Tags`**:类型分类  
  - **`Explore`**:探索内容,只包含题目,收费内容不支持;部分题目加载有顺序限制   
  - **`颜色`**:题目颜色代表题目难度  
  - **`符号`**:题目前`√`与`？`代表当前题目解答状态,探索下有 `$` 开头的为付费或者其他情况下无法查看的   
  
### 菜单  
<p align="center">  
  <img src="https://raw.githubusercontent.com/shuzijun/leetcode-editor/master/doc/menu-3.0.png" alt="menu"/>  
</p>   

- **菜单(在题目上右击出现)**:  
  - **`open question`**:打开题目,在题目上双击也可以打开  
  - **`Submit`**:提交题目  
  - **`Submissions`**:查看提交记录,在弹出的窗口上选择记录查看详情(`Show detail`)  
  - **`Run Code`**:运行代码,默认使用题目的测试用例  
  - **`Testcase`**:自定义测试用例  
  - **`favorite`**:添加或移除收藏
  - **`Clear cache`**:清理当前题目  
