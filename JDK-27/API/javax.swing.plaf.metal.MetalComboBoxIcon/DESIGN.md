# javax.swing.plaf.metal.MetalComboBoxIcon — DESIGN

## 1. API 功能
Metal 组合框箭头图标（`Icon` + `Serializable`）：`getIconWidth`==10、`getIconHeight`==5、`paintIcon`（需 `JComboBox`）。

## 2. Java 用户如何使用
由 Metal L&F 作为组件图标使用；测试构造后断言尺寸与 paintIcon 冒烟（传入对应组件类型）。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalComboBoxIcon.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`MetalComboBoxIconTest.testBlackBox`）
构造 → getIconWidth==10、getIconHeight==5 → paintIcon 到 BufferedImage。
