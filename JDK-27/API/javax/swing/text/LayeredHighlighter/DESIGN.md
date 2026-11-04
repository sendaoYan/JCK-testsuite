# javax.swing.text.LayeredHighlighter — DESIGN

## 1. API 功能
`Highlighter` 的抽象子类，支持"分层"高亮——叶子 View 在绘制文本时先调用 `paintLayeredHighlights` 绘制高亮背景，再绘制文本。方法分组：
- 构造器：`protected LayeredHighlighter()`。
- 抽象方法：`paintLayeredHighlights(Graphics,int,int,Shape,JTextComponent,View)`。
- 继承的 Highlighter 方法（addHighlight/removeHighlight/changeHighlight/getHighlights/...）。

## 2. Java 用户如何使用
通常不直接实现，而是用其具体子类 `DefaultHighlighter`（`new DefaultHighlighter()`），其 `drawsLayeredHighlights` 默认 true，使 LayerPainter 类的高亮走分层绘制路径。`paintLayeredHighlights` 由视图渲染流程调用，业务代码一般只调用 add/change/remove。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`：`public class DefaultHighlighter extends LayeredHighlighter { public DefaultHighlighter() { drawsLayeredHighlights = true; } ... public void paintLayeredHighlights(...) { for (...) { ... lhi.paintLayeredHighlights(...); } } }` —— 主要实现。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/LayeredHighlighter.java`：`public abstract class LayeredHighlighter implements Highlighter { public abstract void paintLayeredHighlights(...); public abstract static class LayerPainter implements Highlighter.HighlightPainter { ... } }`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`addHighlight`）：`HighlightInfo i = (getDrawsLayeredHighlights() && (p instanceof LayeredHighlighter.LayerPainter)) ? new LayeredHighlightInfo() : new HighlightInfo();`。

## 4. 测试用例设计（黑盒部分，`LayeredHighlighterTest.testBlackBox`）
1. `new DefaultHighlighter()` instanceof Highlighter。
2. 无高亮时 `paintLayeredHighlights(null,0,0,null,area,null)` 为 no-op 不抛异常。
3. 经 addHighlight 添加高亮后 getHighlights 数量正确；drawsLayeredHighlights 默认开启。
