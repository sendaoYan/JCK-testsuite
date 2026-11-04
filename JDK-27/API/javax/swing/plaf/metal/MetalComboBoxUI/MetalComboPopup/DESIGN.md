# javax.swing.plaf.metal.MetalComboBoxUI.MetalComboPopup — DESIGN

## 1. API 功能
`MetalComboBoxUI.MetalComboPopup` 是 Metal 观感下组合框下拉弹出层的实现（`extends BasicComboPopup`，`public` 非静态内部类）。它负责下拉列表的弹出、布局与鼠标/键盘交互，把 Metal 的边框、滚动条外观应用到组合框弹出层上。构造需要外围 `MetalComboBoxUI` 实例与目标 `JComboBox`。

## 2. Java 用户如何使用
用户通常不直接实例化本类；它由 `MetalComboBoxUI` 在 `installUI` 时创建。直接使用时：`MetalComboBoxUI ui = new MetalComboBoxUI(); MetalComboBoxUI.MetalComboPopup popup = ui.new MetalComboPopup(cb);`，随后可查询 `popup.isVisible()`、`popup.getList()` 等继承自 `BasicComboPopup` 的行为。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalComboBoxUI.java:402`：`public class MetalComboPopup extends BasicComboPopup`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:289`：将 `"ComboBoxUI"` 映射到 `MetalComboBoxUI`（弹出层随之外观生效）。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalLookAndFeel/Test8039750.java:38`：`UIDefaults table = new MetalLookAndFeel().getDefaults();` 覆盖 Metal 默认值表（含组合框弹出层依赖项）。

## 4. 测试用例设计（黑盒部分，`MetalComboPopupTest.testBlackBox`）
1. 构造 `MetalComboBoxUI` 与 `JComboBox`，`createUI` 返回本委托。
2. 通过 `ui.new MetalComboPopup(cb)` 构造弹出层，断言其为 `MetalComboBoxUI.MetalComboPopup` 实例。
3. 弹出层在未显示时 `isVisible()` 为假；显示/隐藏切换无异常。
4. `uninstallUI` 无异常。
