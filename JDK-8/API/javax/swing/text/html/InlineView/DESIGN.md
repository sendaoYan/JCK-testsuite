# javax.swing.text.html.InlineView — DESIGN

## 1. API 功能
HTML 行内视图（`public class InlineView extends LabelView`），显示行内元素（`<b>`/`<i>`/`<span>`）的文本及样式。核心方法：
- `getAttributes()`：元素属性（含解析后的 CSS 键）。
- `insertUpdate/removeUpdate/changedUpdate`：文档变更响应。
- `getBreakWeight(int axis, float pos, float len)`/`breakView(...)`：折行契约。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为行内标签创建；也可对 content 元素直接 `new InlineView(elem)` 做单元级查询（css 属性暴露、断行权重、碎片化）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/InlineView.java`：继承 LabelView，属性含 CSS.Attribute。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/CSS/4765271/bug4765271.java`：行内样式回归。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/InlineView/bug4623342.java`：InlineView 回归。

## 4. 测试用例设计（黑盒，`InlineViewTest`：testShape + testModelLayer，28 断言）
1. testShape：superclass LabelView、insertUpdate/removeUpdate/changedUpdate/getAttributes/getBreakWeight/breakView 方法存在。
2. 模型层（Metal LaF + 固定 HTML）：bold run 元素身份、offset 往返、属性非 null 且实例稳定、恰 3 个属性、FONT_WEIGHT 定义且值含 "bold"；无样式 run 不定义 FONT_WEIGHT。
3. 折行契约：窄请求（3px/0px）getBreakWeight == BadBreakWeight。
4. breakView：返回非 null 碎片、区间有序、不长于原 run（碎片锚点实现细节，放宽为范围断言）。
