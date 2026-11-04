# javax.swing.DebugGraphics 设计案

## 1. API 功能
`DebugGraphics` 是继承 `java.awt.Graphics` 的调试图形上下文，可开启日志/闪烁/缓冲调试。方法分组：
- 构造：`DebugGraphics()`、`DebugGraphics(Graphics)`、`DebugGraphics(Graphics, JComponent)`。
- 调试选项：`setDebugOptions(int)`、`getDebugOptions()`；常量 `LOG_OPTION/FLASH_OPTION/BUFFERED_OPTION/NONE_OPTION`。
- 闪烁：`setFlashColor/flashColor`、`setFlashTime/flashTime`、`setFlashCount/flashCount`。
- 日志：`setLogStream/logStream`。
- 绘制：`drawRect/fillRect/drawLine/drawOval/drawString` 等（委托底层 `Graphics`）。

## 2. Java 用户如何使用
用户临时开启调试绘制，或通过 `JComponent.setDebugGraphicsOptions` 启用：
```java
DebugGraphics g = new DebugGraphics(realGraphics);
g.setDebugOptions(DebugGraphics.LOG_OPTION | DebugGraphics.FLASH_OPTION);
g.setFlashColor(Color.RED);
```
典型链路：`new DebugGraphics(g)` → `setColor` → `drawRect/fillRect` → `setDebugOptions/getDebugOptions` → `setFlashColor/flashColor`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JComponent.java` 用 `DebugGraphics` 实现组件级调试绘制（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java`）。
- JDK 源码 `javax/swing/RepaintManager.java` 与 `javax/swing/DebugGraphicsInfo.java` 配合管理调试图形上下文。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `java.awt.Graphics`、无接口。
- 行为（黑盒）：`testBlackBox()` 用离屏 `BufferedImage` 覆盖 `setColor/getColor`、多个绘制方法 smoke、`setDebugOptions/getDebugOptions`、`setFlashColor/flashColor`、`setFlashTime/flashTime`、`setFlashCount/flashCount`、`create()`。
