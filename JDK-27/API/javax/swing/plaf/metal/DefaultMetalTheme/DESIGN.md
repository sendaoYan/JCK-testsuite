# javax.swing.plaf.metal.DefaultMetalTheme — DESIGN

## 1. API 功能
Metal 默认主题（`MetalTheme` 子类）：`getName`（返回 "Steel"）、六个字体 getter（`FontUIResource`）。

## 2. Java 用户如何使用
由 `MetalLookAndFeel` 用作默认主题；测试直接构造并读取名称与字体。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/DefaultMetalTheme.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`DefaultMetalThemeTest.testBlackBox`）
构造 → getName=="Steel" → 六个字体 getter 均非空且为 `FontUIResource`。
