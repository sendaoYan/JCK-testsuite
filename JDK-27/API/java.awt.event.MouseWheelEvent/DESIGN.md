# java.awt.event.MouseWheelEvent 行为测试设计

## 1. API 功能
鼠标滚轮事件（继承 MouseEvent）：scrollType（UNIT/BLOCK）、scrollAmount、wheelRotation、preciseWheelRotation、getUnitsToScroll。

## 2. Java 用户如何使用
`new MouseWheelEvent(component, MOUSE_WHEEL, when, mods, x, y, 0, 0, 0, false, WHEEL_UNIT_SCROLL, 3, -1)`；MouseWheelListener 读 getWheelRotation/getUnitsToScroll。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/MouseWheelEvent.java`；`test/jdk/java/awt/event/MouseWheelEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 MouseEvent、常量/构造器。黑盒：构造→回读 scrollType/scrollAmount/wheelRotation/preciseWheelRotation；getUnitsToScroll=amount*rotation；14 参显式 precise；常量精确值；paramString 含 scrollType/scrollAmount/wheelRotation。
对应 `MouseWheelEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
