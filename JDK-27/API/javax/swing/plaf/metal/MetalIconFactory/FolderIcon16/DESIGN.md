# javax.swing.plaf.metal.MetalIconFactory.FolderIcon16 — DESIGN

## 1. API 功能
`MetalIconFactory.FolderIcon16` 是 Metal 观感的 16px 文件夹图标（`implements Icon, Serializable`，`public` 静态嵌套类）。提供 `paintIcon/getIconWidth/getIconHeight/getShift/getAdditionalHeight`，其中 `getShift()` 与 `getAdditionalHeight()` 均返回 0。

## 2. Java 用户如何使用
`MetalIconFactory.FolderIcon16 icon = new MetalIconFactory.FolderIcon16(); icon.paintIcon(comp, g, 0, 0);`。绘制时组件可为任意非空 `Component`（仅读取其 `GraphicsConfiguration`，不可显示时回退 `BufferedImage`）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1674`：`public static class FolderIcon16 implements Icon, Serializable`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1747` 与 `:1754`：`getShift()` 返回 0、`getAdditionalHeight()` 返回 0。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1773`：`TreeFolderIcon extends FolderIcon16`（子类覆写为 shift=-1、附加高度 2）。

## 4. 测试用例设计（黑盒部分，`FolderIcon16Test.testBlackBox`）
1. 构造实例并断言为 `Icon` 与 `Serializable`。
2. `getShift()==0`、`getAdditionalHeight()==0`、宽高非负。
3. `paintIcon` 画到 `BufferedImage`（传入 `JLabel`）无异常。
