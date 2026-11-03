# javax.swing.text.html.BlockView — DESIGN

## 1. API 功能
HTML 块级视图（`public class BlockView extends CompositeView`），是 HTMLEditorKit 对块级 HTML 元素（如 `<p>`、`<h1>`）的默认视图实现。核心方法：
- 构造器：`BlockView(Element elem, int axis)`（axis 通常 `View.Y_AXIS`）。
- 伸缩权重：`getResizeWeight(int axis)` — X 轴返回 1（水平可伸缩），Y 轴返回 0（垂直固定）；非法 axis 抛 `IllegalArgumentException`。
- 对齐：`getAlignment(int axis)` 返回 0。
- 布局/绘制：`layout/paint` 委托 CompositeView 的块布局。
- 属性：`getAttributes()` 返回元素的 AttributeSet。

## 2. Java 用户如何使用
通常不直接实例化。用户 `new JEditorPane()` → `setEditorKit(new HTMLEditorKit())` → `setText("<html>...")`，由 `HTMLEditorKit$HTMLFactory.create` 在构建视图树时创建 BlockView。测试可通过 `editor.getUI().getRootView(editor)` 递归遍历获取实例，再调用 `getResizeWeight/getAlignment/getMaximumSpan` 等查询。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`（内部类 `HTMLFactory.create`）：对块级 `HTML.Tag` 构造 `new BlockView(elem, View.Y_AXIS)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/BlockView.java`：`getResizeWeight/getAlignment` 的轴分支实现。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/CompositeView.java`：BlockView 继承的块布局基类。

## 4. 测试用例设计（黑盒部分，`BlockViewTest.testBlackBox`）
1. 从编辑器视图树递归查找 live BlockView 实例。
2. `getResizeWeight(X_AXIS)==1`、`getResizeWeight(Y_AXIS)==0`。
3. `getAlignment(X_AXIS)==0`。
4. 非法 axis（如 4/5）调用 `getMaximumSpan/getAlignment` → `IllegalArgumentException`。
5. `getAttributes()` 非 null 且实例稳定；span `min <= pref <= max`。
