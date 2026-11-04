# java.awt.event.InputEvent 行为测试设计

## 1. API 功能
所有输入事件（键盘/鼠标）抽象基类，带 when 时间戳、modifiers 新旧两套修饰键、isShiftDown/isControlDown/等状态查询、getModifiersEx/getModifiersExText、getMaskForButton。

## 2. Java 用户如何使用
不直接实例化；经 KeyEvent/MouseEvent 使用，读 getWhen/getModifiersEx/isShiftDown 判断输入状态。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/InputEvent.java`（掩码常量、getModifiersEx 位运算）；`test/jdk/java/awt/event/InputEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public abstract、继承 ComponentEvent、掩码/方法形态。黑盒：经 KeyEvent 实例化→getWhen/getModifiers/getModifiersEx 回读；isShiftDown/isControlDown/isAltDown 组合；consume/isConsumed；9 个掩码常量精确值；getMaskForButton(1..3) 与 (0/4) 抛 IAE；getModifiersExText。
对应 `InputEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
