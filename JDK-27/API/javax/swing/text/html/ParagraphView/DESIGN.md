# javax.swing.text.html.ParagraphView — DESIGN

## 1. API 功能
HTML 段落视图（`public class ParagraphView extends javax.swing.text.ParagraphView`），是 FlowView 的段落子类，负责段落内文本的流式折行排版（懒布局 pool）。关键方法：
- `getResizeWeight(int axis)`：X 轴 1（可折行伸缩）、Y 轴 0。
- `getAlignment(int axis)`：段落对齐。
- `getPreferredSpan/getMinimumSpan/getMaximumSpan`：段落度量（依赖布局）。
- 继承 FlowView 的 `setParent/layout`。

## 2. Java 用户如何使用
由 `HTMLEditorKit$HTMLFactory` 为 `<p>` 等段落标签创建。注意与 `javax.swing.text.ParagraphView` 同名，代码中需用全限定名 `javax.swing.text.html.ParagraphView` 区分。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/ParagraphView.java`：继承 `javax.swing.text.ParagraphView`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/CSS/bug4284162.java`：段落/流式布局回归。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/FlowView/6318524/bug6318524.java`：FlowView 懒布局回归（ParagraphView 继承自 FlowView）。

## 4. 测试用例设计（黑盒部分，`ParagraphViewTest.testBlackBox`）
1. 从视图树递归查找 live `javax.swing.text.html.ParagraphView`（全限定名）。
2. `getResizeWeight(X_AXIS) == 1`、`getResizeWeight(Y_AXIS) == 0`。
3. `getAlignment` 返回有限值；span `min <= pref <= max`。
4. 非法 axis → IAE；`setParent` 可调用。
