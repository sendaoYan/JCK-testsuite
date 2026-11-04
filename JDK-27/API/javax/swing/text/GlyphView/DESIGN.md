# javax.swing.text.GlyphView — DESIGN

## 1. API 功能
可断行的字形视图（`extends View`，implements `TabableView, Cloneable`）。核心方法：
- 字形绘制器：`getGlyphPainter()/setGlyphPainter(GlyphPainter)`（内部抽象类 GlyphPainter 委托排版/绘制/命中）。
- 文本：`getText(int,int)` 返回 Segment、`getFont()/getForeground()/getBackground()`、`getTabExpander()`。
- 样式：`isUnderline()/isStrikeThrough()/isSubscript()/isSuperscript()`。
- 尺寸：`getPreferredSpan/getMinimumSpan/getAlignment`；TabableView：`getTabbedSpan/getPartialSpan`。
- 映射与断行：`modelToView/viewToModel/getNextVisualPositionFrom/breakView/createFragment/getBreakWeight`。

## 2. Java 用户如何使用
通常由 StyledEditorKit 为内容元素创建（LabelView extends GlyphView）；用户可 `new GlyphView(leafElement)` 后 setParent 查询字形宽度/高度，`createFragment(p0,p1)` 断出子片段，`getGlyphPainter()` 获取绘制器。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/LabelView.java`：`extends GlyphView implements TabableView`，为样式化文本标签。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/GlyphView.java` 内部 `GlyphPainter`：默认 `getPainter` 返回 this、`getNextVisualPositionFrom` 处理东西移动。

## 4. 测试用例设计（黑盒部分，`GlyphViewTest.testBlackBox`）
1. 叶元素构造：offsets/document/getElement 一致。
2. 绘制器生命周期：初始 `getGlyphPainter()==null`、`setGlyphPainter(null)` 清空。
3. PlainDocument 上 `getBackground()==null`、无下划线/删除线/上下标。
4. setParent 后 `getFont/getForeground` 非 null；`getText(0,5)` 返回 "hello"；跨度非负、对齐在 [0,1]。
5. `createFragment(0,5)` 返回不同 GlyphView 且偏移正确；`breakView` 非 null、`getBreakWeight` 合法。
6. `modelToView/viewToModel` 单调 + Forward 偏置；`getNextVisualPositionFrom(EAST)` 前进 1。
