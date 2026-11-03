# javax.swing.text.LabelView — DESIGN

## 1. API 功能
样式化文本标签视图（`extends GlyphView`，implements `TabableView`）。核心方法：
- 样式解析（来自 StyledDocument）：`getFont()/getForeground()/getBackground()/isUnderline()/isStrikeThrough()/isSubscript()/isSuperscript()`。
- 尺寸：继承 GlyphView 的 `getPreferredSpan/getMinimumSpan/getAlignment`。
- TabableView：`getTabbedSpan/getPartialSpan`。
- 映射/断行：继承 GlyphView 的 `modelToView/viewToModel/createFragment/breakView/getBreakWeight`。

## 2. Java 用户如何使用
必须配合 StyledDocument（JTextPane）使用，直接对 PlainDocument 叶元素构造会抛 `StateInvariantError`。StyledEditorKit 的 StyledViewFactory 为 Content 元素创建 LabelView；用户 `new LabelView(contentElement)` 后查询字体/前景/下划线等样式。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:315/328`：Content 元素及默认回退均 `new LabelView(elem)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java`：布局时对 `TabableView` 子视图调用 `getTabbedSpan`。

## 4. 测试用例设计（黑盒部分，`LabelViewTest.testBlackBox`）
1. JTextPane 内容元素构造：offsets/document/getElement 一致。
2. 样式解析：`getFont/getForeground` 非 null、`getBackground()==null`、默认无下划线/删除线/上下标。
3. 绘制器生命周期：初始 null、`setGlyphPainter(null)` 清空。
4. setParent 后 X/Y 跨度非负、`getTabbedSpan/getPartialSpan` 非负。
5. `modelToView` 非 null 且 x 单调。
6. 通过 `setCharacterAttributes` 设置下划线/删除线后，新建 LabelView 反映样式。
