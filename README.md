# idea-leetcode-plugin


### 配置
<p align="center">
  <img src="https://github.com/shuzijun/idea-leetcode-plugin/blob/master/doc/setting.png" alt="settings"/>
</p>
 - 配置路径: settings->tools->leetcode plugin <br>
  - URL可选项： leetcode.com与leetcode-cn.com <br>
  - code type: 除部分数据库题目与shell题目外,其他类型已支持 <br>
  - longinName与password 对应URL的登陆账户 <br>
  - file path: 临时文件存放目录 <br>

### 窗口
<p align="center">
  <img src="https://github.com/shuzijun/idea-leetcode-plugin/blob/master/doc/window1.png" alt="window"/>
  <img src="https://github.com/shuzijun/idea-leetcode-plugin/blob/master/doc/window.png" alt="window"/>
</p>
 - 上方四个按钮为：登陆 退出 刷新 清理 <br>
    - - 登录:两个网站的登录帐号不互通，切换网站需配置对应的用户<br>
    - - 退出...<br>
    - - 刷新:在未登录的情况下也可查看刷新加载题目，但是无法提交<br>
    - - 清理:清理配置的缓存目录下的文件，两个网站对应的缓存目录不同，只会清理当前配置的网站下的。部分题目未提交的情况下慎重清理 <br>
 - 中间搜索框:输入内容后回车搜索，再次回车搜索下一个，只会搜索题库下面的<br>
 - 树节点:分别表示全部题目，难度分类，标签分类和探索，探索下只会加载题目，文本不加载，部分收费项目不支持。部分题目有顺序限制，需要先解决前置问题，否则无法加载。<br>
 - 题目颜色代表题目难度 <br>
 - 题目前√与？代表当前题目解答状态,探索下有 $ 开头的为付费或者其他情况下无法查看的 <br>
 
### 菜单
<p align="center">
  <img src="https://github.com/shuzijun/idea-leetcode-plugin/blob/master/doc/menu.png" alt="menu"/>
</p>
 - 在对应的题目上右键,可打开文件，提交答案与测试，默认答案存放在本地缓存下，如需重新获取，可clear后重新打开 <br>
