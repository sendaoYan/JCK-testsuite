# java.awt.event.ActionEvent 行为测试设计

## 1. API 功能
语义动作事件（如按钮点击/菜单选择），带 actionCommand、when 时间戳、modifiers 修饰键，常量 ACTION_PERFORMED=1001。

## 2. Java 用户如何使用
`new ActionEvent(source, ACTION_PERFORMED, "save", when, CTRL_MASK)` 表示动作；监听器 ActionListener.actionPerformed 中读 getActionCommand/getModifiers。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/ActionEvent.java`（paramString）；`test/jdk/java/awt/event/` 及 Swing 各按钮测试。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、常量与 3/4/5 参构造器。黑盒：构造→getID/getActionCommand/getWhen/getModifiers 回读；3/4 参默认 when/modifiers；修饰键常量值；paramString 含 id/cmd/modifiers。
对应 `ActionEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
