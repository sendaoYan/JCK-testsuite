# javax.swing.text.html.parser.ParserDelegator — DESIGN

## 1. API 功能
`extends HTMLEditorKit.Parser`（`implements Serializable`）。负责每次 `parse` 启动一个新的 `DocumentParser`，并持有默认 DTD（html32，按 AppContext 缓存）。
- 构造器：`ParserDelegator()`（触发 `setDefaultDTD()` 加载 html32）。
- 核心：`parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharSet) throws IOException`。

## 2. Java 用户如何使用
`new ParserDelegator()` → `parse(new StringReader("<p>Hi</p>"), callback, false)`，用 `HTMLEditorKit.ParserCallback` 子类收集 `handleStartTag/handleText/handleSimpleTag/handleComment/handleEndOfLineString`。ignoreCharSet=true 时忽略 meta charset 切换。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/HTMLEditorKit/bug4238223.java`：
  ```java
  static class TestParser extends ParserDelegator {
      public void parse(String html) throws Exception {
          Reader r = new StringReader(html);
          super.parse(r, new TestCallback(), false);
          r.close();
      }
  }
  ```
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/ParserDelegator.java`（自用）：`getDefaultDTD()` 用 `DTD.getDTD("html32")` + `dtd.read(html32.bdtd)` 填充默认 DTD。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/parser/Parser/7165725/bug7165725.java`：`import javax.swing.text.html.parser.ParserDelegator;` 与 `HTMLEditorKit` 协作。

## 4. 测试用例设计（黑盒部分，`ParserDelegatorTest.testBlackBox`）
1. `<p>Hello</p>`：start 含 html/head/body/p、end 标签、文本 Hello、EOL=="\n"。
2. `<br>foo`：simple 标签 br、文本 foo。
3. `<!-- note -->`：注释含 note。
4. `ignoreCharSet=true`：仍解析 p 与文本。
5. 多次 parse 各自独立（新 DocumentParser）。
