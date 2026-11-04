# javax.swing.plaf.metal.MetalComboBoxButton — DESIGN

## 1. API 功能
Metal 组合框箭头按钮（`JButton` 子类）：`getComboBox`/`setComboBox`、`getComboIcon`/`setComboIcon`、`isIconOnly`/`setIconOnly`、`isFocusTraversable`（恒 false）。

## 2. Java 用户如何使用
由 `MetalComboBoxUI.createArrowButton()` 创建；测试直接构造并走 setter/getter 往返。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalComboBoxButton.java`（jdk25u-dev）；`MetalComboBoxUI` 内部使用。

## 4. 测试用例设计 + 自动化实现（`MetalComboBoxButtonTest.testBlackBox`）
构造 `MetalComboBoxButton(cb,null,pane,list)` → getComboBox/isIconOnly 初始态 → setIconOnly/setComboIcon 往返 → isFocusTraversable==false → getMinimumSize 非空。
