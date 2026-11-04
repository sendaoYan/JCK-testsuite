# java.text.FieldPosition — 测试设计案

## 1. API 功能
`FieldPosition` 指示 `Format.format(Object, StringBuffer, FieldPosition)` 要回报**哪个字段**在
输出中的起止位置（供 GUI 高亮、提取子串等）。

- 构造：`FieldPosition(int field)`（数字字段常量如 `NumberFormat.INTEGER_FIELD=0`）；
  `FieldPosition(Format.Field attribute)`；`FieldPosition(Format.Field attribute, int field)`
  （属性与常量双标识，字段 id 未给时为 `FIELD_UNDEFINED(-1)`）。
- 查询/设置：`getField()/getFieldAttribute()/getBeginIndex()/getEndIndex()`、
  `setBeginIndex(int)/setEndIndex(int)`（由 format 实现填写）。
- 对象协议：`equals/hashCode`（attribute+field+begin+end 四元组）、`toString`。

## 2. Java 用户如何使用
`FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD); StringBuffer sb = new
StringBuffer(); nf.format(number, sb, fp); int begin = fp.getBeginIndex(), end =
fp.getEndIndex();`；带属性的写法用 `new FieldPosition(DateFormat.Field.YEAR)`，
format 后 `fp.getFieldAttribute()` 仍是同一属性实例。链路：
ctor -> 传入 format(...) -> format 填 begin/end -> 按字段切子串 -> equals 比较两次结果。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/FieldPosition.java`（本地 `~/git/jdk25u-dev`）：
  equals 依次比较 attribute（Objects.equals）、beginIndex、endIndex、field；toString 输出
  `"java.text.FieldPosition[field=..,attribute=..,beginIndex=..,endIndex=..]"`——测试第 2/4 节
  逐字符断言该格式。
- OpenJDK `src/java.base/share/classes/java/text/DecimalFormat.java#format(...)`：
  DecimalFormat 在 format 过程中对 FieldPosition 回调 `setBeginIndex/setEndIndex`，整数部分
  含分组分隔符——第 5 节以 `"1,234,567.89"` 验证 INTEGER/FRACTION 跨度。
- OpenJDK `test/jdk/java/text/DecimalFormat/FormatOffset.java` 等：官方测试读取
  FieldPosition 回报的 offset 提取字段文本。
- Oracle javadoc
  (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/FieldPosition.html)
  的 FieldDemo 示例即 “format -> begin/end -> substring” 用法。

## 4. 测试用例设计 + 自动化实现（FieldPositionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| int 构造 + setter | getField/attribute null/索引默认 0；setBegin/End 读写、可覆盖 |
| equals/hashCode | 同 field 默认索引相等；begin/end/field 任一变化破坏；null/异型 false；自反 |
| 属性构造 | DateFormat.Field.ERA：attribute 保持、field==-1（FIELD_UNDEFINED）；组合构造两者都保留 |
| toString | int 构造精确串；属性构造含 "attribute=era" 与 "field=-1" |
| 真实 format 回报 | DecimalFormat(US) format(1234567.89)：INTEGER_FIELD span==[0,dot)、
  FRACTION 属性 span==[dot+1,len)，且 getFieldAttribute 返回原实例 |

共 5 组场景、约 25 个 check（`testBlackBox()`）。

## 契约深化（本批新增，两侧同构断言）

- 构造（int 字段 / Format.Field）+ format 填充 begin/end 精确（1234.56 的 INTEGER [0,6)/FRACTION [7,9) 含符号位）
- setBeginIndex/setEndIndex 手动；getField/getFieldAttribute；toString 精确格式
- equals/hashCode（field+attribute+begin+end）
