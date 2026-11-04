# javax.swing.text.html.InlineView — DESIGN

## 1. API 功能
HTML 行内视图（`public class InlineView extends LabelView`），显示行内 HTML 元素（如 `<b>`、`<i>`、`<span>`）的文本及其样式。继承 LabelView 的文本绘制、伸展（stretch）与对齐能力。核心方法：
- `getAttributes()`：返回元素属性。
- `getResizeWeight(int axis)` / `getAlignment(int axis)`：行内文本对齐（Y 轴对齐基线）。
- `getPreferredSpan/getMinimumSpan/getMaximumSpan`：文本度量。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为行内标签创建。用户不直接实例化；在物化后的编辑器视图树中可作为 FlowView 行的叶子出现，可递归遍历获取。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/InlineView.java`：继承 LabelView，调整 Y 轴对齐与伸展。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/CSS/4765271/bug4765271.java`：行内样式回归。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/InlineView/bug4623342.java`：InlineView 回归。

## 4. 测试用例设计（黑盒部分，`InlineViewTest.testBlackBox`）
1. 从视图树递归查找 live InlineView 实例（`<b>text</b>` 内容物化为行内叶子）。
2. `getAttributes()` 非 null 且稳定。
3. `getResizeWeight`、`getAlignment` 返回有限值（非法 axis → IAE）。
4. 各轴 span `min <= pref <= max` 且非负。
