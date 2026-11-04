# javax.swing.text.html.parser.DTDConstants — DESIGN

## 1. API 功能
纯常量接口，供 DTD/Element/AttributeList/Entity/Parser 复用。分四组：
- 内容类型：CDATA=1…RCDATA=16、EMPTY=17、MODEL=18、ANY=19。
- 属性修饰：FIXED=1、REQUIRED=2、CURRENT=3、CONREF=4、IMPLIED=5。
- 实体类型：PUBLIC=10、SDATA=11、PI=12、STARTTAG=13、ENDTAG=14、MS=15、MD=16、SYSTEM=17。
- 标志位：GENERAL=65536、DEFAULT=131072、PARAMETER=262144。

## 2. Java 用户如何使用
实现该接口（或直接 `DTDConstants.X`）以拿到类型/修饰/实体标志常量，用于解析/构造 DTD 数据模型时比较 `Element.getType()`、`AttributeList.getType()/getModifier()`、`Entity.getType()/isGeneral()/isParameter()`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Entity.java`：`getType()` 返回 `type & 0xFFFF`，`isGeneral()` 判 `(type & GENERAL) != 0`，`isParameter()` 判 `(type & PARAMETER) != 0`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Element.java`：`isEmpty()` 判 `type == EMPTY`；`name2type` 映射 CDATA/RCDATA/EMPTY/ANY。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DTD.java`：`defEntity("#RE", GENERAL, '\r')` 用 GENERAL 标志建内置实体。

## 4. 测试用例设计（黑盒部分，`DTDConstantsTest.testBlackBox`）
逐条断言全部 35 个常量精确值（内容类型 19 个、修饰 5 个、实体类型 9 个、标志位 3 个），确保 ABI 常量与公开文档一致。

## 5. 深化批（契约覆盖增量）
1. 反射全量形状：恰好 35 个常量字段，全部 public static final int。
2. 分组性质：内容类型 1..19 连续且互异；修饰词 FIXED<REQUIRED<CURRENT<CONREF<IMPLIED 严格递增；实体类型 10..17 互异；GENERAL/DEFAULT/PARAMETER 均为 2 的幂且位于 16 位类型区之上（可与类型位或组合不碰撞）。
3. 实现者关系：AttributeList/Element/Entity/DTD/Parser 均 instanceof DTDConstants。
