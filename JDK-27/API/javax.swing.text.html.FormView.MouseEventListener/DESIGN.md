# javax.swing.text.html.FormView.MouseEventListener — DESIGN

## 1. API 功能
`protected class MouseEventListener extends MouseAdapter`，是 FormView 内部的鼠标事件监听器，在用户点击表单控件（submit/reset 按钮）时触发提交或重置动作。它是 FormView 的内部实现细节，非公开导出（public=false、protected=true）。

## 2. Java 用户如何使用
不直接实例化。FormView 在 `createComponent()` 中对生成的控件调用 `addMouseListener(new MouseEventListener())`，由 Swing 事件分发线程在点击时分发 `mouseClicked`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormView.java`：`createComponent()` 末尾 `c.addMouseListener(new MouseEventListener())`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/TestFormViewNPE.java`：间接覆盖表单鼠标交互的 NPE 回归。

## 4. 测试用例设计（黑盒部分，`MouseEventListenerTest.testBlackBox`）
1. 通过反射实例化 `MouseEventListener`（protected 构造器），断言 `instanceof MouseAdapter`。
2. 构造空 `MouseEvent` 调用 `mouseClicked` 不抛异常（无实际控件上下文时安全）。
