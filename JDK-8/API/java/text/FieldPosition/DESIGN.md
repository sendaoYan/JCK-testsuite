# java.text.FieldPosition 测试设计案（JDK-8）

> 覆盖目标：完整契约（三个构造器、四元组 equals/hashCode、toString、真实 format 回报 span），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`FieldPosition(int)`、`FieldPosition(Format.Field)`、`FieldPosition(Format.Field, int)`。
- 查询/设置：`getField/getFieldAttribute/getBeginIndex/getEndIndex`、`setBeginIndex/setEndIndex`。
- 对象协议：`equals/hashCode`（attribute+field+begin+end）、`toString`。

## 2. Java 用户如何使用

- `new FieldPosition(NumberFormat.INTEGER_FIELD)` 交给 `format(x, sb, fp)`，format 填 begin/end，
  用于切字段子串或高亮。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/FieldPosition.java`。
- JDK-27 同目录 DESIGN.md（toString/equals 行为一致）。

## 4. 测试用例设计（映射到 FieldPositionTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：三个公开构造器、方法签名 | testWhiteBox |
| P0 | int 构造 + getters/setters | testBlackBox |
| P0 | equals/hashCode（field/begin/end 参与） | testBlackBox |
| P0 | 属性构造器（FIELD_UNDEFINED=-1） | testBlackBox |
| P0 | toString 精确格式 | testBlackBox |
| P0 | DecimalFormat 真实回报 INTEGER/FRACTION span | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- 属性构造器 `getField()==-1`（FIELD_UNDEFINED）。
- `toString()` 为 `java.text.FieldPosition[field=...,attribute=...,beginIndex=...,endIndex=...]`。

## 注意事项

- 固定 Locale.US；无墙钟/随机；headless-safe 不加 `@key`。
