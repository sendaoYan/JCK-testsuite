# javax.swing.plaf.metal.MetalFileChooserUI — DESIGN

## 1. API 功能
`MetalFileChooserUI` 是 Metal 观感下 `JFileChooser` 的 UI 委托（`extends BasicFileChooserUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`；生命周期 `installUI/uninstallUI/installComponents/uninstallComponents`；文件名/目录名访问 `getFileName/setFileName/getDirectoryName/setDirectoryName`；布局 `getPreferredSize/getMinimumSize/getMaximumSize`；以及 `createListSelectionListener/createPropertyChangeListener`、`ensureFileIsVisible/rescanCurrentDirectory/valueChanged`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JFileChooser` 自动挂载本委托。直接使用时：`JFileChooser fc = new JFileChooser(); MetalFileChooserUI ui = new MetalFileChooserUI(fc); ui.installUI(fc); ui.setFileName("a.txt"); ui.uninstallUI(fc);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:51`：`public class MetalFileChooserUI extends BasicFileChooserUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:905` 与 `:915`：`getDirectoryName()` 恒返回 `null`、`setDirectoryName` 为空实现（JDK 内 `PENDING` 占位）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:291`：将 `"FileChooserUI"` 映射到 `MetalFileChooserUI`。

## 4. 测试用例设计（黑盒部分，`MetalFileChooserUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(fc)` 无异常。
2. `setFileName("jck.txt")` 后 `getFileName()` 回读一致；`setDirectoryName` 无异常且 `getDirectoryName()` 为 `null`（占位契约）。
3. 三个尺寸方法返回非空且非负。
4. 两个监听器工厂返回非空；`ensureFileIsVisible/rescanCurrentDirectory/valueChanged` 无异常。
5. `installComponents/uninstallComponents/uninstallUI` 无异常。
