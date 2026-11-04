# javax.swing.plaf.metal.MetalBorders.RolloverButtonBorder — DESIGN

## 1. API 功能
悬停按钮边框（继承 `ButtonBorder`，仅覆写 `paintBorder`）。

## 2. Java 用户如何使用
由 `MetalBorders` 工厂产出或直接构造；测试构造后走 getBorderInsets/isBorderOpaque/paintBorder。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalBorders.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`RolloverButtonBorderTest.testBlackBox`）
构造 RolloverButtonBorder → UIResource 标记 → getBorderInsets(null) 非空 → isBorderOpaque==false → paintBorder 到 BufferedImage。
