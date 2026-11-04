# javax.swing.plaf.metal.MetalIconFactory — DESIGN

## 1. API 功能
`MetalIconFactory` 是 Metal 观感图标的静态工厂（`implements Serializable`，非抽象）。它缓存并返回各类内部图标：文件选择器图标、树图标（`FileIcon16/FolderIcon16/TreeControlIcon/TreeFolderIcon/TreeLeafIcon`）、单选/复选按钮图标、菜单箭头图标、滑杆缩略图标、内部框架图标等。另含 `DARK`/`LIGHT` 常量区分树控件的明暗外观。

## 2. Java 用户如何使用
用户通过静态访问器获取图标并绘制：`Icon i = MetalIconFactory.getTreeFolderIcon(); i.paintIcon(comp, g, 0, 0);`，或 `new MetalIconFactory()` 取得可序列化的工厂实例。多数图标绘制到 `BufferedImage` 时无需真实显示组件。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:81`：`public class MetalIconFactory implements Serializable`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalIconFactory.java:294` 与 `:315`：`getTreeFolderIcon()/getTreeLeafIcon()` 返回对应树图标。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalIconFactory/bug4952462.java:53-54`：`MetalLookAndFeel.setCurrentTheme(new OceanTheme()); UIManager.setLookAndFeel(...)` 后验证单选按钮图标绘制。

## 4. 测试用例设计（黑盒部分，`MetalIconFactoryTest.testBlackBox`）
1. `new MetalIconFactory()` 为 `Serializable`；`DARK != LIGHT`。
2. 19 个静态图标访问器均返回非空且宽高非负。
3. 组件无关的图标（文件选择器/树/文件夹/叶/树控件）画到 `BufferedImage`（传入不可显示的 `JLabel`）无异常；需要特定组件类型（单选/复选/按钮/菜单/滑杆/内部框架）的图标由各自独立测试覆盖绘制。
