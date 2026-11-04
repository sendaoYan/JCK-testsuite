# javax.swing.plaf.metal.MetalFileChooserUI.FilterComboBoxRenderer — DESIGN

## 1. API 功能
`MetalFileChooserUI.FilterComboBoxRenderer` 是文件选择器过滤组合框的渲染器（`extends DefaultListCellRenderer`，`public` 非静态内部类）。它把过滤器对象渲染为组合框列表项。构造需要外围 `MetalFileChooserUI` 实例（进而需要 `JFileChooser`）。

## 2. Java 用户如何使用
本类为 public 内部类，可在持有 `MetalFileChooserUI` 实例时直接构造：`MetalFileChooserUI ui = new MetalFileChooserUI(new JFileChooser()); MetalFileChooserUI.FilterComboBoxRenderer r = ui.new FilterComboBoxRenderer();`，再调用继承自 `DefaultListCellRenderer` 的渲染方法。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:1133`：`public class FilterComboBoxRenderer extends DefaultListCellRenderer`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalFileChooserUI.java:925`：`createDirectoryComboBoxRenderer` 返回 `DirectoryComboBoxRenderer`（同族渲染器工厂）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:291`：`"FileChooserUI"` 映射到 `MetalFileChooserUI`。

## 4. 测试用例设计（黑盒部分，`FilterComboBoxRendererTest.testBlackBox`）
1. 反射确认类为 public、非抽象、继承 `DefaultListCellRenderer`。
2. 通过 `ui.new FilterComboBoxRenderer()` 构造，断言其为 `ListCellRenderer`。
3. `getListCellRendererComponent` 返回非空组件且无异常。
