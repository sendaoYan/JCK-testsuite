# javax.swing.plaf.metal.MetalFileChooserUI.FileRenderer — DESIGN

## 1. API 功能
`MetalFileChooserUI.FileRenderer` 是文件选择器列表/目录的文件单元格渲染器（`extends DefaultListCellRenderer`，`protected` 非静态内部类）。它把文件对象渲染为带图标的列表项。构造器为 protected，仅包内可访问。

## 2. Java 用户如何使用
本类为 protected 内部实现，由 `MetalFileChooserUI` 内部使用；包外用户无法直接实例化。测试只能通过反射验证其 `ListCellRenderer` 类型关系。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:584`：`protected class FileRenderer extends DefaultListCellRenderer`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:1133`：`public class FilterComboBoxRenderer extends DefaultListCellRenderer`（同族公开渲染器，对比可见）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:291`：`"FileChooserUI"` 映射到 `MetalFileChooserUI`。

## 4. 测试用例设计（黑盒部分，`FileRendererTest.testBlackBox`）
1. 反射确认类为 protected、非抽象、继承 `DefaultListCellRenderer`。
2. 断言 `ListCellRenderer.class.isAssignableFrom(c)`，退化为类型关系验证。
