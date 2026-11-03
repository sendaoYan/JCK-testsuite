# javax.swing.plaf.metal.MetalIconFactory.FileIcon16 — DESIGN

## 1. API 功能
`MetalIconFactory.FileIcon16` 是 Metal 观感的 16px 文件图标（`implements Icon, Serializable`，`public` 静态嵌套类）。提供 `paintIcon(Component, Graphics, int, int)`、`getIconWidth/getIconHeight`，以及 `getShift()`（返回 0）与 `getAdditionalHeight()`（返回 0）两个公开布局常量方法。

## 2. Java 用户如何使用
`MetalIconFactory.FileIcon16 icon = new MetalIconFactory.FileIcon16();` 后调用 `icon.paintIcon(comp, g, 0, 0)` 绘制；`getIconWidth/getIconHeight` 查询尺寸。绘制时组件可为任意非空 `Component`（仅读取其 `GraphicsConfiguration`，不可显示时回退 `BufferedImage`）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1798`：`public static class FileIcon16 implements Icon, Serializable`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1866` 与 `:1873`：`getShift()` 返回 0、`getAdditionalHeight()` 返回 0。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1883`：`TreeLeafIcon extends FileIcon16`（其子类覆写移位/高度）。

## 4. 测试用例设计（黑盒部分，`FileIcon16Test.testBlackBox`）
1. 构造实例并断言为 `Icon` 与 `Serializable`。
2. `getShift()==0`、`getAdditionalHeight()==0`、宽高非负。
3. `paintIcon` 画到 `BufferedImage`（传入 `JLabel`）无异常。
