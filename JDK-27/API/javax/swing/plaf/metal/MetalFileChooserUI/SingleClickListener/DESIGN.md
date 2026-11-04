# javax.swing.plaf.metal.MetalFileChooserUI.SingleClickListener — DESIGN

## 1. API 功能
`MetalFileChooserUI.SingleClickListener` 是文件选择器的单击监听器（`extends MouseAdapter`，`protected` 非静态内部类）。它在列表上单击时切换到所选目录/文件。构造器为 protected，仅包内可访问。

## 2. Java 用户如何使用
本类为 protected 内部实现，由 `MetalFileChooserUI` 内部注册到目录/文件列表；包外用户无法直接实例化。测试只能通过反射验证其 `MouseListener` 类型关系。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:568`：`protected class SingleClickListener extends MouseAdapter`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:1257`：`DirectoryComboBoxAction`（同族目录切换动作，相关交互）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:291`：`"FileChooserUI"` 映射到 `MetalFileChooserUI`。

## 4. 测试用例设计（黑盒部分，`SingleClickListenerTest.testBlackBox`）
1. 反射确认类为 protected、非抽象、继承 `MouseAdapter`。
2. 断言 `MouseListener.class.isAssignableFrom(c)`（`MouseAdapter` 实现 `MouseListener`），退化为类型关系验证。
