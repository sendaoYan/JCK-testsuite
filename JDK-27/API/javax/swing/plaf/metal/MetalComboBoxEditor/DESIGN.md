# javax.swing.plaf.metal.MetalComboBoxEditor — DESIGN

## 1. API 功能
Metal 组合框编辑器（继承 `BasicComboBoxEditor`）：`getEditorComponent`、`setItem`/`getItem`、`selectAll`。

## 2. Java 用户如何使用
由 `MetalComboBoxUI.createEditor()` 创建，内部持有 `JTextField`；测试直接构造并走 setItem/getItem。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalComboBoxEditor.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`MetalComboBoxEditorTest.testBlackBox`）
构造 → `getEditorComponent` 非空 → `setItem/getItem` 文本往返 → `selectAll`。
