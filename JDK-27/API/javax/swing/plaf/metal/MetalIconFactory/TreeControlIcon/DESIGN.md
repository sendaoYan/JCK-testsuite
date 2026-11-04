# javax.swing.plaf.metal.MetalIconFactory.TreeControlIcon — DESIGN

## 1. API 功能
`MetalIconFactory.TreeControlIcon` 是 Metal 观感的树展开/折叠控件图标（`implements Icon, Serializable`，`public` 静态嵌套类）。构造参数 `isCollapsed` 决定折叠/展开外观；提供 `paintIcon` 与公开的 `paintMe(Component, Graphics, int, int)`。宽高均为 18。

## 2. Java 用户如何使用
`MetalIconFactory.TreeControlIcon icon = new MetalIconFactory.TreeControlIcon(true); icon.paintIcon(comp, g, 0, 0);` 或直接调用 `icon.paintMe(comp, g, 0, 0)`。组件可为任意非空 `Component`（读取 `GraphicsConfiguration` 与组件方向）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1907`：`public static class TreeControlIcon implements Icon, Serializable`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1987`：`paintMe` 绘制圆与展开/折叠手柄。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:325`：`getTreeControlIcon(boolean)` 工厂方法。

## 4. 测试用例设计（黑盒部分，`TreeControlIconTest.testBlackBox`）
1. 以 `false`/`true` 构造两个图标，断言为 `Icon`/`Serializable`。
2. 宽高非负（18×18）。
3. `paintIcon`（light）与 `paintMe`（dark）画到 `BufferedImage`（传入 `JLabel`）均无异常。
