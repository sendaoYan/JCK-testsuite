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

## 5. 深化批（契约覆盖增量）
1. **缓存契约**：`getDTD` 本身**不写缓存**——未注册名字连续两次返回不同实例；`putDTDHash` 注册后 `getDTD` 命中同实例；再次 put 同名则替换；`getDTD` 一律先 `toLowerCase`（混合大小写 key 经 getDTD 不可达，putDTDHash 原样存）；`getDTD(null)`/`putDTDHash(null,..)` NPE。
2. **新建 DTD 契约**（protected 构造器经子类暴露）：null name 合法（getName/toString==null）；预置 15 元素精确有序表 `#pcdata,html,meta,base,isindex,head,body,applet,param,p,title,style,link,script,unknown`；预置 3 实体 #RE(CR)/#RS(LF)/#SPACE(space)；`unknown` 元素 type=EMPTY、omitEnd=true；getElement(int) 0..14 与 elements 向量一致。
3. **html32 契约**：归档后 elements.size()==88、elementHash 同；字符键实体表 `&`->amp、`<`->lt、`>`->gt、`"`->quot、U+00A0->nbsp；`getEntity(-1)` 返回 null（不抛）；名字查找大小写敏感（`BODY` 新建另一元素）；body idx=6、html idx=1、p idx=9；field/getter 一致（oStart/oEnd/type/atts/content）。
4. define 族：defineEntity 重名返回原实例且**数据不替换**；defEntity(name,type,int) 公有、char 装箱；defineElement 新建/既有（同实例就地改型）；defineAttributes 装表并**按名隐建元素**、传 null 清空；defineEntity/defineElement/defineAttributes 的 null 名 NPE。
5. 越界：getElement(-1)/getElement(size)/getElement(999999) -> ArrayIndexOutOfBoundsException；read(null) NPE；DTD 非 Serializable、instanceof DTDConstants。
6. 执行顺序注意：testDeepHtml32 的 pristine 计数断言必须在旧 testBlackBox（会新建 2 个元素）之前运行——main 中已重排。
