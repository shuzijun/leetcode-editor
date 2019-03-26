# leetcode-editor  

## 使用方式  

### 安装  
- 通过插件库安装 https://plugins.jetbrains.com/plugin/12132-leetcode-editor  
- 下载文件安装 https://github.com/shuzijun/leetcode-editor/blob/master/doc/leetcode-editor.zip  

### 配置  

>>>>![settings](https://github.com/shuzijun/leetcode-editor/blob/master/doc/setting.png "settings")  
 
- 配置路径: `File` -> `settings`->`tools`->`leetcode plugin`  
  - URL可选项： leetcode.com与leetcode-cn.com  
  - code type: 除部分数据库题目与shell题目外,其他类型已支持  
  - longinName与password 对应URL的登陆账户  
  - file path: 临时文件存放目录  
  
### 窗口(主窗口右下角![icon](https://github.com/shuzijun/leetcode-editor/blob/master/resources/image/LeetCodeIcon.png))  
  
>>>>![settings](https://github.com/shuzijun/leetcode-editor/blob/master/doc/window1.png)![settings](https://github.com/shuzijun/leetcode-editor/blob/master/doc/window2.png)  
  
- 上方四个按钮为：登陆 退出 刷新 清理  
  - 登录:两个网站的登录帐号不互通，切换网站需配置对应的用户  
  - 退出...  
  - 刷新:在未登录的情况下也可查看刷新加载题目，但是无法提交  
  - 清理:清理配置的缓存目录下的文件，两个网站对应的缓存目录不同，只会清理当前配置的网站下的。部分题目未提交的情况下慎重清理  
- 中间搜索框:输入内容后回车搜索，再次回车搜索下一个，只会搜索题库下面的  
- 树节点:分别表示全部题目，难度分类，标签分类和探索，探索下只会加载题目，文本不加载，部分收费项目不支持。部分题目有顺序限制，需要先解决前置问题，否则无法加载。  
  - 题目颜色代表题目难度  
  - 题目前√与？代表当前题目解答状态,探索下有 $ 开头的为付费或者其他情况下无法查看的  
 
### 菜单  
>>>>![menu](https://github.com/shuzijun/leetcode-editor/blob/master/doc/menu.png "menu")  
  
  - 在对应的题目上右键,可打开文件，提交答案与测试，默认答案存放在本地缓存下，如需重新获取，可clear后重新打开  
