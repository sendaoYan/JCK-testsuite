# javax.swing.plaf.metal.MetalComboBoxUI.MetalPropertyChangeListener — DESIGN

## 1. API 功能
`MetalComboBoxUI.MetalPropertyChangeListener` 是 Metal 组合框的属性变化监听器（`extends BasicComboBoxUI.PropertyChangeHandler`，`public` 非静态内部类）。它把组合框的 `model/editor/editable/enabled/font/renderer` 等属性变更转发到内部 `Handler`，以同步 UI 状态。构造需要外围 `MetalComboBoxUI` 实例。

## 2. Java 用户如何使用
通常由 `MetalComboBoxUI` 在安装时注册到组合框。直接使用时：`MetalComboBoxUI ui = new MetalComboBoxUI(); MetalComboBoxUI.MetalPropertyChangeListener l = ui.new MetalPropertyChangeListener(); l.propertyChange(new PropertyChangeEvent(cb, "editable", null, Boolean.TRUE));`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalComboBoxUI.java:217`：`public class MetalPropertyChangeListener extends BasicComboBoxUI.PropertyChangeHandler`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicComboBoxUI.java:1783`：`Handler.propertyChange` 将非 editor 来源强制转换为 `JComboBox` 并分发属性名。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:289`：`"ComboBoxUI"` 映射到 `MetalComboBoxUI`。

## 4. 测试用例设计（黑盒部分，`MetalPropertyChangeListenerTest.testBlackBox`）
1. 反射确认类为 public、非抽象、继承 `BasicComboBoxUI$PropertyChangeHandler`。
2. 通过 `ui.new MetalPropertyChangeListener()` 构造监听器，断言其为 `PropertyChangeListener`。
3. 以 `JComboBox` 为源、未知属性名派发 `propertyChange`，验证强制转换路径不抛异常、且未安装 UI 状态下安全（无操作）。
