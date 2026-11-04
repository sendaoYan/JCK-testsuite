# javax.swing.text.FlowView.FlowStrategy — DESIGN

## 1. API 功能
流式布局策略（public static 内部类，`extends Object`）。核心方法：
- `layout(FlowView)`：重建物理行（`layoutRow` + `createRow` + `replace`）。
- 通知：`insertUpdate/removeUpdate/changedUpdate(FlowView, DocumentEvent, Rectangle)`（记录 damage、触发 repaint/preferenceChanged）。
- 逻辑视图：`getLogicalView(FlowView)`（protected，返回 fv.layoutPool）。
- 断行：`layoutRow(FlowView,int,int)`、`adjustRow`、`createView`（protected）。

## 2. Java 用户如何使用
默认策略由 FlowView 构造时创建，`layout`/`insertUpdate` 由 FlowView 转发调用；用户可子类化重写 `createView/layoutRow` 定制断行。`insertUpdate(fv, null, null)` 是 loadChildren 的合成初始化调用。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/FlowView.java:103`：`strategy = new FlowStrategy()`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TextLayoutStrategy.java:55`：`extends FlowView.FlowStrategy` 定制 i18n 布局。

## 4. 测试用例设计（黑盒部分，`FlowStrategyTest.testBlackBox`）
1. `new FlowView.FlowStrategy()` 构造非 null。
2. 子类暴露 `getLogicalView(fv)` 返回 `fv.layoutPool`（手动 setPool 后一致性）。
3. `insertUpdate(fv, null, null)`（loadChildren 合成调用）不抛异常。
