# javax.swing.text.DefaultHighlighter — DESIGN

## 1. API 功能
`LayeredHighlighter`（即 `Highlighter`）的默认实现，用彩色背景标记文档区间。方法分组：
- 构造器：`DefaultHighlighter()`。
- 高亮管理：`addHighlight(int,int,HighlightPainter)`（返回 tag）、`removeHighlight(Object)`、`removeAllHighlights()`、`changeHighlight(Object,int,int)`、`getHighlights()`。
- 分层绘制：`setDrawsLayeredHighlights(boolean)/getDrawsLayeredHighlights()`（默认 true）、`paintLayeredHighlights(...)`。
- 生命周期：`install(JTextComponent)`（会清空已有高亮）/ `deinstall`；`paint(Graphics)`。
- 静态字段：`DefaultPainter`（`new DefaultHighlightPainter(null)` 的 LayerPainter 单例）。

## 2. Java 用户如何使用
`new DefaultHighlighter()` → `install(area)` → `Object tag = addHighlight(p0, p1, painter)` 添加高亮 → `getHighlights()` 读回 `Highlight[]` → `changeHighlight(tag, a, b)` 移动区间 → `removeHighlight(tag)` 删除 → `removeAllHighlights()` 清空。painter 用 `DefaultHighlighter.DefaultPainter` 或 `new DefaultHighlightPainter(Color)`。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultCaret.java`（`setSelectionVisible`）：`selectionTag = h.addHighlight(p0, p1, p); ... h.changeHighlight(selectionTag, p0, p1); ... h.removeHighlight(selectionTag);` —— 选区可见性就是靠 DefaultHighlighter 的 add/change/remove 实现的。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`addHighlight`）：`if (p0 < 0) throw new BadLocationException("Invalid start offset", p0); if (p1 < p0) throw new BadLocationException("Invalid end offset", p1);` —— 非法区间抛 BadLocationException。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`install`）：`component = c; removeAllHighlights();` —— 安装时清空已有高亮。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`：`public static final LayeredHighlighter.LayerPainter DefaultPainter = new DefaultHighlightPainter(null);`。

## 4. 测试用例设计（黑盒部分，`DefaultHighlighterTest.testBlackBox`）
1. drawsLayeredHighlights 默认 true、往返；getHighlights 初始为空。
2. install 后 addHighlight 两个区间 → getHighlights 数量/偏移/painter 按插入顺序断言。
3. changeHighlight 更新偏移；removeHighlight 删除一个。
4. addHighlight 非法区间（p0<0、p1<p0）抛 BadLocationException。
5. removeAllHighlights 清空；再次 install 清空已有高亮。
6. DefaultPainter 静态字段非 null。
