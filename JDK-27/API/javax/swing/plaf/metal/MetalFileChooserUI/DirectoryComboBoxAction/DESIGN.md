# javax.swing.plaf.metal.MetalFileChooserUI.DirectoryComboBoxAction — DESIGN

## 1. API 功能
`MetalFileChooserUI.DirectoryComboBoxAction` 是文件选择器目录组合框的 `Action`（`extends AbstractAction`，`protected` 非静态内部类）。它把目录切换动作封装为 `actionPerformed(ActionEvent)`，供目录组合框在用户选择目录时调用。构造器为 protected，仅包内/子类可访问。

## 2. Java 用户如何使用
本类为 protected 内部实现，普通用户无法从包外直接实例化；它由 `MetalFileChooserUI` 内部创建并注册到目录组合框。测试只能通过反射验证其类型关系与 `actionPerformed` 方法存在。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:1257`：`protected class DirectoryComboBoxAction extends AbstractAction`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:1266`：重写 `actionPerformed(ActionEvent)` 处理目录切换。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:291`：`"FileChooserUI"` 映射到 `MetalFileChooserUI`（本类随之外观生效）。

## 4. 测试用例设计（黑盒部分，`DirectoryComboBoxActionTest.testBlackBox`）
1. 反射确认类为 protected、非抽象、继承 `AbstractAction`。
2. 断言 `Action.class.isAssignableFrom(c)`，且存在 `actionPerformed(ActionEvent)` 方法（构造器 protected 无法包外实例化，退化为类型关系验证）。
