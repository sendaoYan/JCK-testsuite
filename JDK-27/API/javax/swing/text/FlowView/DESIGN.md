# javax.swing.text.FlowView — DESIGN

## 1. API 功能
流式布局抽象视图（`extends BoxView`）。核心方法：
- 轴：`getFlowAxis()`（主轴正交方向，默认）、`getFlowSpan(int)/getFlowStart(int)`（行约束，默认 layoutSpan=MAX / 0）。
- 布局池：`layoutPool`（LogicalView）、`strategy`（FlowStrategy）、`createRow()`（abstract）。
- 布局：`layout(int,int)` 委托 `strategy.layout(this)` 重建行；`loadChildren` 建 LogicalView 并 `strategy.insertUpdate(this,null,null)`。
- 通知：`insertUpdate/removeUpdate/changedUpdate` 转发到 layoutPool 与 strategy。

## 2. Java 用户如何使用
抽象类，子类（ParagraphView）实现 `createRow()`；`new FlowView(element, axis)` 后 setParent 触发 LogicalView 与 FlowStrategy 初始化，`layout` 按 `getFlowSpan/getFlowStart` 约束把逻辑子视图流式排入物理行。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java:58`：`ParagraphView extends FlowView implements TabExpander`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/FlowView.java:379`：`createRow()` 由 ParagraphView 实现为 `new Row(...)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TextLayoutStrategy.java:55`：`class TextLayoutStrategy extends FlowView.FlowStrategy`。

## 4. 测试用例设计（黑盒部分，`FlowViewTest.testBlackBox`）
1. 子类构造：`getAxis==X_AXIS`、`getFlowAxis==Y_AXIS`、offsets。
2. `getFlowSpan(0)==Integer.MAX_VALUE`、`getFlowStart(0)==0`。
3. `createRow()`（通过子类暴露）返回非 null 行；未布局时 `getViewCount()==0`。
