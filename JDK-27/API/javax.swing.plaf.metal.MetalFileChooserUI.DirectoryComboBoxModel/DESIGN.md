# javax.swing.plaf.metal.MetalFileChooserUI.DirectoryComboBoxModel — DESIGN

## 1. API 功能
`MetalFileChooserUI.DirectoryComboBoxModel` 是文件选择器目录组合框的数据模型（`extends AbstractListModel<Object>` 并 `implements ComboBoxModel<Object>`，`protected` 非静态内部类）。它维护文件系统目录层级，提供 `getSize/getElementAt/setSelectedItem/getSelectedItem` 以及内部 `getDepth`。构造器为 protected，仅包内可访问。

## 2. Java 用户如何使用
本类为 protected 内部实现，普通用户无法从包外直接实例化；它由 `MetalFileChooserUI` 创建并挂载到目录组合框。测试只能通过反射验证其模型类型关系。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:996`：`protected class DirectoryComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object>`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicFileChooserUI.java:512` 与 `:527`：`getDirectoryName/setDirectoryName` 的 Basic 层实现（目录模型相关）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:291`：`"FileChooserUI"` 映射到 `MetalFileChooserUI`。

## 4. 测试用例设计（黑盒部分，`DirectoryComboBoxModelTest.testBlackBox`）
1. 反射确认类为 protected、非抽象、继承 `AbstractListModel`、实现 `ComboBoxModel`。
2. 断言 `ComboBoxModel` 与 `ListModel` 均可赋值（`ComboBoxModel` 继承 `ListModel`），退化为类型关系验证。
