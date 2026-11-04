# javax.swing.plaf.metal.MetalBorders — DESIGN

## 1. API 功能
Metal 边框工厂：`getButtonBorder`/`getTextBorder`/`getTextFieldBorder`/`getToggleButtonBorder`/`getDesktopIconBorder`。

## 2. Java 用户如何使用
由 `MetalLookAndFeel` 提供组件边框；测试直接调用五个静态工厂并断言非空与类型。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalBorders.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`MetalBordersTest.testBlackBox`）
五个静态工厂均返回非空 Border；getButtonBorder 返回 ButtonBorder；构造 MetalBorders 实例。
