# java.text.ParsePosition 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造/游标、equals/hashCode、toString、真实解析链路、ParseException 衔接），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`ParsePosition(int index)`。
- 游标：`getIndex()/setIndex(int)`、`getErrorIndex()/setErrorIndex(int)`（默认 errorIndex=-1）。
- 对象协议：`equals/hashCode`（由 index+errorIndex 决定）、`toString`。

## 2. Java 用户如何使用

- `new ParsePosition(0)` → `format.parseObject(s, pos)` → 读 `pos.getIndex()` 续解析；失败读
  `pos.getErrorIndex()`。失败时 `Format.parseObject(String)` 抛 `ParseException` 且
  `getErrorOffset()==errorIndex`。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/ParsePosition.java`。
- JDK-27 同目录 DESIGN.md（toString/equals 行为一致）。

## 4. 测试用例设计（映射到 ParsePositionTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：非抽象非 final、单公开构造器、方法签名 | testWhiteBox |
| P0 | 构造/游标/errorIndex 默认与复位 | testBlackBox |
| P0 | equals/hashCode（index+errorIndex 参与） | testBlackBox |
| P0 | toString 精确格式 + 默认/负值/MAX_VALUE | testBlackBox |
| P0 | NumberFormat 真实解析：前缀消费、index 推进、失败 errorIndex | testBlackBox |
| P0 | 失败解析 → ParseException.getErrorOffset 衔接 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- `toString()` 为 `java.text.ParsePosition[index=...,errorIndex=...]`；负 index、MAX_VALUE 不校验。
- 失败解析保持 index 不动、`errorIndex` 记录失败位置。

## 注意事项

- 固定 Locale.US；无墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- index/errorIndex 全生命周期（默认 -1、setIndex/setErrorIndex、负值允许无校验）
- toString 精确 `java.text.ParsePosition[index=N,errorIndex=M]`
- 由 DecimalFormat/SimpleDateFormat 解析驱动（成功前进/失败 errorIndex 记录/ParseException.getErrorOffset 镜像）
