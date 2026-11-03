# javax.swing.plaf.metal.MetalFileChooserUI.FilterComboBoxModel — DESIGN

## 1. API 功能
`MetalFileChooserUI.FilterComboBoxModel` 是文件选择器过滤组合框的数据模型（`extends AbstractListModel<Object>` 并 `implements ComboBoxModel<Object>, PropertyChangeListener`，`protected` 非静态内部类）。它维护文件过滤器的可选列表并监听过滤属性变化。构造器为 protected，仅包内可访问。

## 2. Java 用户如何使用
本类为 protected 内部实现，由 `MetalFileChooserUI` 创建并挂载到过滤组合框；包外用户无法直接实例化。测试只能通过反射验证其模型与监听器类型关系。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:1166`：`protected class FilterComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object>, PropertyChangeListener`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicFileChooserUI.java:512`：Basic 层 `getDirectoryName()`（过滤/目录模型相关）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:291`：`"FileChooserUI"` 映射到 `MetalFileChooserUI`。

## 4. 测试用例设计（黑盒部分，`FilterComboBoxModelTest.testBlackBox`）
1. 反射确认类为 protected、非抽象、继承 `AbstractListModel`、实现 `ComboBoxModel` 与 `PropertyChangeListener`。
2. 断言 `ComboBoxModel` 与 `PropertyChangeListener` 均可赋值，退化为类型关系验证。
