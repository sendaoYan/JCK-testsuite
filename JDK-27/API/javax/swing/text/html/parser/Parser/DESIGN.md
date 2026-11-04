# javax.swing.text.html.parser.Parser — DESIGN

## 1. API 功能
简单 DTD 驱动的 HTML 解析器基类（`implements DTDConstants`）。读取 HTML 流并在遇到标签/数据时回调可覆写的 protected 方法。
- 构造器：`Parser(DTD dtd)`。
- 入口：`parse(Reader in) throws IOException`、`parseDTDMarkup() throws IOException`。
- 可覆写：`handleStartTag(TagElement)/handleEndTag(TagElement)/handleEmptyTag(TagElement)/handleText(char[])/handleComment(char[])/handleTitle(char[])/handleError(int,String)`；辅助 `getCurrentLine()/getCurrentPos()/getBlockStartPosition()/makeTag(...)/getAttributes()/flushAttributes()`。

## 2. Java 用户如何使用
子类化 Parser，构造时传 DTD，覆写 `handleStartTag/handleEndTag/handleText/handleEmptyTag/handleComment` 捕获事件，调用 `parse(new StringReader(html))` 驱动。默认实现中 html/head/body 为隐含（fictional）标签。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DocumentParser.java`（JDK 子类）：`public DocumentParser(DTD dtd) { super(dtd); }`，覆写各 handle 后转发给 `HTMLEditorKit.ParserCallback`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Parser.java`（自用）：`parse()` 中 `parseContent()` 后 `while (stack != null) endTag(true)` 收尾，`finally` 中再 `handleEndTag(stack.tag)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/parser/Parser/7165725/bug7165725.java`：通过 `HTMLEditorKit` 间接驱动 Parser 测试连续 script 标签。

## 4. 测试用例设计（黑盒部分，`ParserTest.testBlackBox`）
1. 子类捕获 `<p>Hello</p>`：start 序列 html/head/body/p、fictional 标志、文本 Hello、end 标签集合、`getCurrentLine()==1`。
2. `<br>foo`：handleEmptyTag 捕获 br、文本 foo。
3. `<!-- note -->`：handleComment 含 note、`getCurrentPos()>=0`。

## 5. 深化批（契约覆盖增量）
1. Parser 级整表事件序列（Seq 子类 `S:name*/E:/P:/T:/C:/!:`，* 表 fictional）：`<!--c--><p>Hi <b>there</b><br></p>` 的 14 事件精确表；实体在 handleText 前解码；同一 Parser 实例可二次 parse（状态复位）。
2. 行/位置计数：新实例 getCurrentLine()==0/getCurrentPos()==0；单行解析后 ==1；两处换行后 ==3；解析结束后计数器保持末值。
3. protected 面：`dtd` 字段即构造器实参；`strict` 默认 false；`makeTag(elem)` 非 fictional、`makeTag(elem,true)` fictional、**不缓存**（两次调用不同实例）、getHTMLTag 映射到 HTML.Tag 单例；`getAttributes()/flushAttributes()` 在回调内可用、flush 后属性数归零。
4. 异常语义（实测）：`parse(null)` **不抛**——内部 NPE 被 `catch(Exception)` 吃掉、以 handleError("exception java.lang.NullPointerException …") 报告；`parseDTDMarkup()` 未设 reader -> NPE。
