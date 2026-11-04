# javax.swing.plaf.metal.MetalCheckBoxIcon — DESIGN

## 1. API 功能
Metal 复选框图标（`Icon` + `UIResource` + `Serializable`）：`getIconWidth`/`getIconHeight`（均 13）、`paintIcon`（需 `JCheckBox`）。

## 2. Java 用户如何使用
由 Metal L&F 作为组件图标使用；测试构造后断言尺寸与 paintIcon 冒烟（传入对应组件类型）。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalCheckBoxIcon.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`MetalCheckBoxIconTest.testBlackBox`）
构造 → getIconWidth==13、getIconHeight==13 → paintIcon 到 BufferedImage。
