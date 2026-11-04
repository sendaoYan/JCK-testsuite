# javax.swing.plaf.metal.MetalIconFactory.TreeLeafIcon — DESIGN

## 1. API 功能
`MetalIconFactory.TreeLeafIcon` 是 Metal 观感的树叶节点图标（`extends FileIcon16`，`public` 静态嵌套类）。覆写 `getShift()` 返回 2 与 `getAdditionalHeight()` 返回 4，其余绘制复用 `FileIcon16`。

## 2. Java 用户如何使用
`MetalIconFactory.TreeLeafIcon icon = new MetalIconFactory.TreeLeafIcon(); icon.paintIcon(comp, g, 0, 0);`。绘制时组件可为任意非空 `Component`（仅读取其 `GraphicsConfiguration`）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1883`：`public static class TreeLeafIcon extends FileIcon16`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1888` 与 `:1889`：`getShift()` 返回 2、`getAdditionalHeight()` 返回 4。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:315`：`getTreeLeafIcon()` 返回本类实例。

## 4. 测试用例设计（黑盒部分，`TreeLeafIconTest.testBlackBox`）
1. 构造实例并断言为 `FileIcon16` 与 `Icon`。
2. `getShift()==2`、`getAdditionalHeight()==4`、宽高非负。
3. `paintIcon` 画到 `BufferedImage`（传入 `JLabel`）无异常。
