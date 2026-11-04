# javax.swing.text.html.parser.DocumentParser — DESIGN

## 1. API 功能
HTML 文档解析器（`extends Parser`）。把 HTML 流解析后按 HTMLEditorKit 回调模型通知：隐含标签带 `IMPLIED` 属性，`handleSimpleTag` 处理空标签，`handleComment` 处理注释，末尾 `handleEndOfLineString`。
- 构造器：`DocumentParser(DTD dtd)`。
- 核心：`parse(Reader in, HTMLEditorKit.ParserCallback callback, boolean ignoreCharSet) throws IOException`。

## 2. Java 用户如何使用
`new DocumentParser(DTD.getDTD("html32"))`，传 `StringReader` 与自定义 `HTMLEditorKit.ParserCallback` 子类，覆写 `handleStartTag/handleText/handleSimpleTag/handleEndTag/handleComment/handleEndOfLineString`，驱动解析并捕获文档结构。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/ParserDelegator.java`（自用）：
  ```java
  public void parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharSet) throws IOException {
      new DocumentParser(getDefaultDTD()).parse(r, cb, ignoreCharSet);
  }
  ```
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/HTMLEditorKit/bug4238223.java`：`super.parse(r, new TestCallback(), false)`，回调断言 `handleStartTag/handleText/handleComment` 的位置与内容。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DocumentParser.java`（自写文档）：`<p>blah` 会产生隐含 html/head/body + p + 文本 + 收尾回调。

## 4. 测试用例设计（黑盒部分，`DocumentParserTest.testBlackBox`）
1. `<p>Hello <b>world</b></p>`：start 序列 html/head/body/p/b、隐含标志（html/head/body true、p false）、end 标签集合、两个文本块、EOL=="\n"。
2. `<br>foo`：simple 标签 br、文本 foo。
3. `<!-- note -->`：注释回调含 note。
4. `ignoreCharSet=true`：仍正常产出 p 与文本、EOL 不变。

## 5. 深化批（契约覆盖增量）——解析器回调契约（本包核心）
1. **整表回调序列断言**：`SeqCB` 把每个回调记成 `S:tag@pos[attr=..]/E:/P:/T:/C:/!:$:` 字符串，与期望 List 整表 equals。锚表覆盖 注释/隐含骨架/实体解码/属性/简单标签/嵌套/位置/eol，含 `!:invalid.tagatt class p ?` 精确错误串与 `S:p@55[align=center][class=x&y]` 属性表。
2. 骨架与错误恢复矩阵：`<p>x<p>y` 隐含 html/head/body + 隐式 `</p>`；stray `</p>` 只报 `end.extra.tag`；`<b><i>x</b></i>` 报 `end.missing`+`unmatched.endtag` 后补齐；未知标签 `<foobar>` 报 `tag.unrecognized` 且闭合端以 `P:foobar[endtag=true]` 送达；`<!DOCTYPE html>` 静默跳过但骨架仍生成。
3. 属性契约：名字一律小写；**枚举值属性（align、input.type）值转小写，非枚举值保大小写**（href=AbC.DEF 保留）；布尔属性 `CHECKED` -> `checked=checked`；属性值内实体解码（class="x&amp;y" -> x&y）；非法枚举值追加 `invalid.tagattval` 错误；回调收到的 AttributeSet 可变（addAttribute 生效）。
4. 文本/实体：`&amp;&lt;&gt;&#65;` 解码；`&unknown;` 按字面保留；`&nbsp;` -> U+00A0；标签间空白折叠成单个空格文本；`
`/`` 在正文中折叠、eol 报第一个；`<pre>` 保留空白换行；`<script>` 内容走 handleComment 而非 handleText；title 文本经 handleText 送达，第二个 title 触发 `tag.unexpected`。
5. charset：`<head><meta http-equiv="charset" content="utf-8">` 且 ignoreCharSet=false -> parse 抛 `ChangedCharSetException`（getCharSetSpec()=="utf-8"）；ignoreCharSet=true 正常解析；`Content-Type: text/html` 不抛。
6. 异常语义（全部实测）：null reader **不向调用者传播**（内部 NPE 被 `Parser.parse` 的 `catch(Exception)` 吃掉、以 `handleError("exception java.lang.NullPointerException ..")` 报告，仍送达 eol 事件）；null callback NPE 传播；回调 RuntimeException 同样被吞并报告；Reader 的 IOException 原样传播。
7. 复用语义：同一 DocumentParser 连续 parse 状态复位（文本/结构正常），但**位置计数器不归零**（第二次解析 offset 接着增长）——断言 min(第二次) > max(第一次)。

## 版本注记（JDK-8 侧）
- 与 JDK-27 的公开 API 逐项一致（javap -public 双侧比对）；本包已知可观察差异仅 Element.serialVersionUID（见 Element/DESIGN.md），另有 NPE/CCE 异常消息形态差异（JDK-14+ helpful NPE），测试一律只断言异常类型。
