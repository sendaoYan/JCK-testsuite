# javax.swing.plaf.metal.MetalIconFactory.TreeFolderIcon — DESIGN

## 1. API 功能
`MetalIconFactory.TreeFolderIcon` 是 Metal 观感的树文件夹图标（`extends FolderIcon16`，`public` 静态嵌套类）。覆写 `getShift()` 返回 -1（无垂直移位）与 `getAdditionalHeight()` 返回 2，其余绘制复用 `FolderIcon16`。

## 2. Java 用户如何使用
`MetalIconFactory.TreeFolderIcon icon = new MetalIconFactory.TreeFolderIcon(); icon.paintIcon(comp, g, 0, 0);`。绘制时组件可为任意非空 `Component`（仅读取其 `GraphicsConfiguration`）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1773`：`public static class TreeFolderIcon extends FolderIcon16`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:1779` 与 `:1780`：`getShift()` 返回 -1、`getAdditionalHeight()` 返回 2。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:294`：`getTreeFolderIcon()` 返回本类实例。

## 4. 测试用例设计（黑盒部分，`TreeFolderIconTest.testBlackBox`）
1. 构造实例并断言为 `FolderIcon16` 与 `Icon`。
2. `getShift()==-1`、`getAdditionalHeight()==2`、宽高非负。
3. `paintIcon` 画到 `BufferedImage`（传入 `JLabel`）无异常。
