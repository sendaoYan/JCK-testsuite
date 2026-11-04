# javax.swing.text.html.HTMLEditorKit.ParserCallback — DESIGN

## 1. API 功能
HTML 解析事件回调类（public 无参构造器，非抽象）。`HTMLEditorKit.Parser.parse` 解析时回调：`handleStartTag`/`handleEndTag`/`handleSimpleTag`/`handleText`/`handleComment`/`handleError`/`handleEndOfLineString`，最后 `flush()`。默认实现全部为 no-op，供子类覆写。静态常量 `IMPLIED`（Object 哨兵）表示解析器推断出的隐含标签。`HTMLDocument.HTMLReader` 是典型子类，把事件落成文档元素。

## 2. Java 用户如何使用
`new ParserCallback()` 直接使用（默认 no-op），或匿名子类覆写 `handleStartTag/handleText/handleEndTag/...` 记录解析事件，再交给 `Parser.parse(reader, callback, ignoreCharSet)` 驱动；解析完成后调 `flush()`。是自定义 HTML 提取/校验的基础。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java`：`HTMLReader extends ParserCallback`，覆写 `handleStartTag/handleEndTag/handleSimpleTag/handleText/handleComment`，构建元素树。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`read()` 内 `ParserCallback receiver = hdoc.getReader(pos); p.parse(in, receiver, ...); receiver.flush();`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/ParserDelegator.java`：`parse` 内对 DTD 元素触发 `callback.handleStartTag(...)`/`handleText(...)`/`handleEndTag(...)`。

## 4. 测试用例设计（黑盒部分，`ParserCallbackTest.testBlackBox`）
1. 默认 ParserCallback 各 handle 方法与 flush 均可安全调用、不抛异常。
2. 静态 `IMPLIED` 非空。
3. 用真实 parser（经 HTMLEditorKit 子类 getParser）驱动 `RecordingCallback` 解析 `<html><head></head><body>hi<br><b>x</b></body></html>`：事件序列以 `start:html` 开头、以 `end:html` 结尾，并依次含 `start:body`、`text:hi`、`simple:br`、`start:b`、`text:x`、`end:b`。
