# javax.swing.text.WrappedPlainView — DESIGN

## 1. API 功能
可换行的纯文本视图（`extends PlainView`）。核心方法：
- 构造器：`WrappedPlainView(Element)`、`WrappedPlainView(Element, boolean wordWrap)`。
- 尺寸/布局：`getPreferredSpan`（按可用宽度换行后的行数×行高）、`getMinimumSpan/getMaximumSpan`、`setSize`、`calculateBreakPosition`。
- 映射：`modelToView/viewToModel`（考虑换行后的多行布局）、`getNextVisualPositionFrom`（换行处南北移动）。
- 继承 PlainView 的 `nextTabStop/getTabbedSpan` 制表逻辑。

## 2. Java 用户如何使用
`BasicTextAreaUI` 在换行模式下创建 `new WrappedPlainView(elem, area.getWrapStyleWord())`；用户可 `new WrappedPlainView(rootElement)` 后 setParent 查询多行尺寸，或从 `JTextArea(lineWrap=true).getUI().getRootView(tc).getView(0)` 取得真实实例。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextAreaUI.java:156`：换行时 `v = new WrappedPlainView(elem, area.getWrapStyleWord())`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/WrappedPlainView.java`：自身实现 `calculateBreakPosition` 断行算法。

## 4. 测试用例设计（黑盒部分，`WrappedPlainViewTest.testBlackBox`）
1. 两个构造器均可实例化，`getStartOffset/getEndOffset` 与根元素一致。
2. `nextTabStop` 与 PlainView 相同的 8 字符网格步进。
3. 真实 `JTextArea(lineWrap=true)` 根子视图 instanceof WrappedPlainView；限制宽度布局后 Y 跨度 > 0（至少一行）。
4. `modelToView/viewToModel` 在多行分配矩形上非 null 且 x 单调。
