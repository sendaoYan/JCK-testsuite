# javax.swing.text.Utilities — DESIGN

## 1. API 功能
`Utilities` 是 Swing 文本的静态工具方法集合（视图/文本坐标换算）。本测试覆盖无需图形设备的方法分组：
- 行定位：`getRowStart(JTextComponent,int)`、`getRowEnd(...)`。
- 垂直导航：`getPositionAbove(...)`/`getPositionBelow(...)`（含 int/float 重载）。
- 词边界：`getWordStart`/`getWordEnd`/`getNextWord`/`getPreviousWord`（基于 BreakIterator）。
- 段落元素：`getParagraphElement(JTextComponent,int)`。

## 2. Java 用户如何使用
`JTextArea ta = ...; ta.setSize(w,h);` 后 `Utilities.getRowStart(ta, offs)` 找行首、`getWordStart/getWordEnd` 选词、`getPositionAbove/Below` 上下行导航、`getParagraphElement` 取所在段落。词方法使用 `c.getLocale()` 的 BreakIterator，需固定 Locale 保证确定性。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`：光标移动动作中 `begOffs = Utilities.getWordStart(target, offs)`、`endOffs = Utilities.getWordEnd(target, offs)`、`offs = Utilities.getNextWord/getPreviousWord/getRowStart/getRowEnd(target, offs)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/Utilities.java`：`getWordStart` 内部 `BreakIterator words = BreakIterator.getWordInstance(c.getLocale()); words.setText(seg);`。

## 4. 测试用例设计（黑盒部分，`UtilitiesTest.testBlackBox`）
`JTextArea`（两行 `"hello world\nfoo bar baz\n"`，`setSize(800,200)`、`setLocale(Locale.US)`）：
1. `getRowStart/getRowEnd` 按换行分界（0/6 同首行，13 次行；行末 11/23）。
2. `getWordStart/getWordEnd/getNextWord/getPreviousWord` 词边界（"world" 6..11）。
3. `getPositionAbove(13,0)==0`、`getPositionBelow(0,0)==12`。
4. `getParagraphElement` 首行/次行起始偏移 0/12。
5. 越界偏移（`doc.length()+5`）→ `BadLocationException` 且 `offsetRequested()` 精确。
