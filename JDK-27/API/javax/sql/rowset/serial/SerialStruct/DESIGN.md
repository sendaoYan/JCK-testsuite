# SerialStruct 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialStruct` 是 `java.sql.Struct` 的离线可序列化实现，保存 SQL 结构化类型的类型名与属性数组（JDK 源码 `SerialStruct.java`）。

- 构造器：`SerialStruct(Struct, Map)`、`SerialStruct(SQLData, Map)`。Struct 构造会先 `System.out.println` 打印类型名（实现遗留的调试输出）；SQLData 构造通过 `SQLOutputImpl` 收集 `writeSQL` 写出的属性。
- 读取：`getSQLTypeName()`、`getAttributes()`、`getAttributes(Map)`（均返回属性数组副本）。
- 内部 `mapToSerial` 会把属性中的 Struct/SQLData/Blob/Clob/Ref/Array 递归替换为对应 Serial 类型。
- 对象语义：`equals`、`hashCode`、`clone`。

## 2. Java 用户如何使用
```java
SerialStruct ss = new SerialStruct(struct, map);   // 或 new SerialStruct(sqlData, map)
String tn = ss.getSQLTypeName();
Object[] attrs = ss.getAttributes();
Object[] attrs2 = ss.getAttributes(map);
SerialStruct copy = (SerialStruct) ss.clone();
```
组合链路：`new(Struct|SQLData, map) → getSQLTypeName → getAttributes/getAttributes(map) → equals/clone`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialStructTests.java`：用 `StubStruct` 与 `SuperHero(SQLData)` 两条构造路径验证 `getSQLTypeName`/`getAttributes`/`getAttributes(map)` 返回相同属性、clone 相等。
- JDK 实现 `SerialStruct.java`：确认 Struct 构造含调试 println；`mapToSerial` 递归序列化嵌套 Struct/Blob/Clob/Ref/Array。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialStructTest.testBlackBox`）覆盖：
- 主链路：Struct 构造与 SQLData 构造 → getSQLTypeName/getAttributes/getAttributes(map) → 两种路径相等 → equals/hashCode/clone。
- 边界：不同 SQL 类型名不相等；嵌套 Struct 属性被转换为 SerialStruct。
- 用内存 `StubStruct`/`HeroData`；headless，不加 `@key`。Struct 构造的调试 println 不影响 jtreg 判定。
