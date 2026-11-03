# javax.swing.text.html.parser.DTD — DESIGN

## 1. API 功能
SGML/HTML DTD 描述（`implements DTDConstants`）。公开字段 `name/elements/elementHash/entityHash` 及 `pcdata/html/meta/base/isindex/head/body/applet/param/p/title` 等元素快捷引用；`FILE_VERSION`。
- 工厂：`getDTD(String)`（小写化并按 AppContext 缓存）、`putDTDHash(String, DTD)`。
- 查询：`getName()/getElement(String)/getElement(int)/getEntity(String)/getEntity(int)/toString()`。
- 定义：`defineEntity(String,int,char[])`、`defineElement(...)`、`defineAttributes(...)`、`defEntity(...)`、`read(DataInputStream)`。

## 2. Java 用户如何使用
`DTD.getDTD("html32")` 拿 DTD → `getElement("p")` 拿元素 → `getAttribute(...)`/`getContent()`；`getEntity("amp")` 拿实体；`defineElement/defineEntity` 动态扩展。注意 `getDTD` 只建空 DTD，实际 html32 内容由 `ParserDelegator` 读 `html32.bdtd` 填充。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/ParserDelegator.java`（自用）：
  ```java
  _dtd = DTD.getDTD(nm); // nm = "html32"
  dtd = createDTD(_dtd, nm); // dtd.read(new DataInputStream(...html32.bdtd...)); DTD.putDTDHash(name, dtd)
  ```
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/parser/Test8017492.java`：`DTD dtd = DTD.getDTD("dtd"); dtd.elements = ...; dtd.getElement("element");`
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DTD.java`（自用）：`defineElement` 先 `getElement(name)` 再回填 type/oStart/oEnd/content 等。

## 4. 测试用例设计（黑盒部分，`DTDTest.testBlackBox`）
1. `FILE_VERSION==1`；`getDTD("html32")` 非空且 getName/toString=="html32"。
2. `getDTD("HTML32")` 小写化复用同一缓存实例。
3. `getDTD("html401")` 非空且含 body/html/p/title 核心字段。
4. `getElement("body")`：name/type=MODEL/非空/有 content/`getElement(index)` 往返；`br` EMPTY；未知元素自动创建。
5. `getEntity("amp")`：name/字符串 "&"/general/非 parameter；`getEntity((int)'&')` 字符索引等价；未知实体 null。
6. `defineEntity` 幂等；`defineElement` 回填 shape；`defineAttributes` 安装属性。
