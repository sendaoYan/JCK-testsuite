# jdk.swing.interop.DragSourceContextWrapper 设计案

## 1. API 功能
拖拽源上下文抽象类。

## 2. Java 用户如何使用
嵌入场景子类化。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/DragSourceContextWrapper.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract；黑盒降级。

## 5. 契约深化（JDK-27）

- 抽象面四方法精确：quitSecondaryEventLoop / setNativeCursor / startDrag / startSecondaryEventLoop；public 构造器收 DragGestureEvent。
- convertModifiersToDropAction 静态纯函数全表（实测 JDK-27）：无修饰→MOVE（支持集含 MOVE 时），否则回退 COPY；BUTTON1/BUTTON2/BUTTON3/SHIFT→MOVE、CTRL→COPY、CTRL|SHIFT→NONE；修饰位与支持集按位与后逐级择优；支持集为 0 恒 NONE。
