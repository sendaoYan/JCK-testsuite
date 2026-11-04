# javax.swing.text.Highlighter.HighlightPainter — DESIGN

## 1. API 功能
高亮绘制器接口，负责在指定区间绘制高亮。方法分组：
- 绘制：`paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c)`（p0/p1 为模型偏移，bounds 为包围盒）。

## 2. Java 用户如何使用
实现该接口（或直接用 `DefaultHighlighter.DefaultHighlightPainter`），作为 `Highlighter.addHighlight(p0, p1, painter)` 的 painter 参数。`Highlight.getPainter()` 读回同一实例。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/Highlighter.java`：`public interface HighlightPainter { void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c); }`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`DefaultHighlightPainter.paint`）：`TextUI mapper = c.getUI(); Rectangle p0 = mapper.modelToView(c, offs0); ... g.fillRect(...);` —— 通过 UI 的 modelToView 计算填充矩形。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/LayeredHighlighter.java`：`LayerPainter implements Highlighter.HighlightPainter` —— 分层绘制器是 HighlightPainter 的子接口。

## 4. 测试用例设计（黑盒部分，`HighlightPainterTest.testBlackBox`）
1. 匿名实现 paint 记录调用与偏移参数（验证接口可调用）。
2. `DefaultHighlighter.DefaultHighlightPainter` instanceof HighlightPainter，getColor 正确。
3. 作为 painter 加入高亮器后按同一性读回。

## 5. 契约深化（本轮追加）
（13 断言）：接口仅 paint(Graphics,int,int,Shape,JTextComponent)，不继承 Serializable（序列化由实现自行负责）；DefaultHighlightPainter 构造后颜色保留、paint 签名可用、不覆写 equals/hashCode（identity 相等：自身相等、不同实例必不等）；DefaultHighlightPainter 同时是 LayeredHighlighter.LayerPainter。
