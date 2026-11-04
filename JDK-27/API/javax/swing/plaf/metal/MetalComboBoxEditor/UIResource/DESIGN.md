# javax.swing.plaf.metal.MetalComboBoxEditor.UIResource — DESIGN

## 1. API 功能
`MetalComboBoxEditor` 的 `UIResource` 标记子类：仅加 `UIResource` 接口，行为同父类。

## 2. Java 用户如何使用
`MetalComboBoxUI.createEditor()` 返回此类型；测试构造并验证 `UIResource` 标记 + setItem/getItem。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalComboBoxEditor.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`UIResourceTest.testBlackBox`）
构造 → instanceof `UIResource` → setItem/getItem 往返。
