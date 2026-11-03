# javax.swing.JCheckBox — DESIGN

## 1. API 功能
复选框（`extends javax.swing.JToggleButton`，implements `Accessible`）。核心方法分组：
- 构造器：`JCheckBox()`、`JCheckBox(String)`、`JCheckBox(String, boolean selected)`、`JCheckBox(Icon)`、`JCheckBox(Action)`。
- 选中态：`setSelected/isSelected`、`doClick()`（切换并触发 ItemEvent + ActionEvent）。
- 边框：`setBorderPainted/isBorderPainted`、`setBorderPaintedFlat/isBorderPaintedFlat`。
- 继承：`setText/setMnemonic/setActionCommand/setEnabled` 等 AbstractButton 方法。

## 2. Java 用户如何使用
`new JCheckBox("Enable")` → `addItemListener(e -> ...)` → `setSelected(true)` 或 `doClick()` 切换；`new JCheckBox("Preset", true)` 预选中。禁用后 `doClick()` 不改变选中态。`setBorderPaintedFlat` 控制平面绘制外观。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicFileChooserUI.java`：`new JCheckBox(...)` + `setSelected` 表达布尔选项。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JCheckBox/`：`doClick` 切换与 ItemEvent SELECTED/DESELECTED 断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JToggleButton.java`：选中态经 `ToggleButtonModel` 触发 `ItemEvent`。

## 4. 测试用例设计（黑盒部分，`JCheckBoxTest.testBlackBox`）
1. 构造文本 + 默认未选中。
2. setSelected(true/false) 往返 + ItemEvent SELECTED/DESELECTED 记录 + 冗余 setSelected 静默。
3. `new JCheckBox("Preset", true)` 预选中 + doClick 切换为 false 并触发 DESELECTED。
4. mnemonic char/int 往返。
5. borderPainted/borderPaintedFlat 往返。
6. text/tooltip/actionCommand 链路；enabled 链：禁用 doClick 不改选中态、恢复后可用。
7. 装入 JFrame 后 displayable/visible 断言。
