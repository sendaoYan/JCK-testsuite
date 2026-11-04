# javax.swing.plaf.metal.MetalBorders.ScrollPaneBorder — DESIGN

## 1. API 功能
滚动面板边框（`AbstractBorder` + `UIResource`）。

## 2. Java 用户如何使用
由 `MetalBorders` 工厂产出或直接构造；测试构造后走 getBorderInsets/isBorderOpaque/paintBorder。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalBorders.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`ScrollPaneBorderTest.testBlackBox`）
构造 ScrollPaneBorder → UIResource 标记 → getBorderInsets(null) 非空 → isBorderOpaque==false → paintBorder 到 BufferedImage。
