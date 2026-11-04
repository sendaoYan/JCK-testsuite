# javax.swing.plaf.metal.MetalScrollButton — DESIGN

## 1. API 功能
`MetalScrollButton` 是 Metal 观感下滑杆两端箭头按钮的实现（`extends BasicArrowButton`，非抽象）。方法分组：构造（方向 + 宽度 + 是否独立）、`setFreeStanding(boolean)` 切换独立模式、`paint(Graphics)` 绘制箭头与高亮、`getPreferredSize()` 返回按钮尺寸。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JScrollBar` 自动使用本按钮。直接使用时：`MetalScrollButton b = new MetalScrollButton(BasicArrowButton.WEST, 16, false); b.setFreeStanding(true); b.setSize(30,30); b.paint(g);`。其 paint 依赖父容器，需挂到 `JPanel` 下再绘制。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalScrollButton.java:54`：`public class MetalScrollButton extends BasicArrowButton`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalScrollButton.java:85`：`public void setFreeStanding(boolean freeStanding)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalScrollBarUI.java:153`：`decreaseButton = new MetalScrollButton(orientation, scrollBarWidth, isFreeStanding)` 创建减少按钮。

## 4. 测试用例设计（黑盒部分，`MetalScrollButtonTest.testBlackBox`）
1. 构造方向为 `WEST` 的按钮，断言非空。
2. `setFreeStanding(true)` 无异常。
3. 挂到 `JPanel` 并设置尺寸后 `paint` 画到 `BufferedImage` 无异常；`getPreferredSize` 返回非空。
