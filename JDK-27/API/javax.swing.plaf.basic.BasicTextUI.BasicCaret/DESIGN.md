# javax.swing.plaf.basic.BasicTextUI.BasicCaret — DESIGN

## 1. API 功能
`DefaultCaret` 的 `UIResource` 子类（无自有声明方法，继承 `getBlinkRate`/`setBlinkRate`，默认 500）。

## 2. Java 用户如何使用
由 `BasicTextUI.createCaret()`/`createHighlighter()` 创建；测试直接构造（静态嵌套类）并走行为断言。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`BasicCaretTest.testBlackBox`）
构造 → UIResource/Caret 标记 → getBlinkRate 非负（默认 0）→ setBlinkRate 往返。
