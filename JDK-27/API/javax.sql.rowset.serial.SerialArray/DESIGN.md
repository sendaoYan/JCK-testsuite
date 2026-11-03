# SerialArray 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialArray` 是 `java.sql.Array` 的离线可序列化实现，保存数组元素副本与 SQL 基类型（JDK 源码 `SerialArray.java`）。

- 构造器：`SerialArray(Array)`、`SerialArray(Array, Map)`（map 为 null 或 array 为 null 抛 SQLException；`getArray()` 返回 null 也抛 SQLException）。
- 读取：`getArray()`、`getArray(Map)`、`getArray(long index, int count)`、`getArray(long index, int count, Map)`（index 0-based）。
- 元数据：`getBaseType()`、`getBaseTypeName()`。
- `getResultSet()` 及三个重载：均抛 `SerialException`（cause 为 UnsupportedOperationException）。
- 生命周期/对象语义：`free()`（后续 no-op，free 后读取抛 SerialException）、`equals`、`hashCode`、`clone`。

## 2. Java 用户如何使用
```java
Array a = ...; // 驱动提供的 Array
SerialArray sa = new SerialArray(a);
int bt = sa.getBaseType();            // java.sql.Types.VARCHAR 等
String btName = sa.getBaseTypeName();
Object[] all = (Object[]) sa.getArray();
Object[] slice = (Object[]) sa.getArray(1, 2); // index 0-based
sa.free();                            // 之后 getArray/getBaseType 抛 SerialException
```
组合链路：`new(Array) → getBaseType/getBaseTypeName → getArray/getArray(index,count) → equals/clone → free`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialArrayTests.java`：覆盖 `getArray()`/`getArray(map)`/`getArray(1,2)`、`getResultSet` 抛 SerialException、free 后各方法抛 SerialException、clone/序列化相等。
- JDK 实现 `SerialArray.java`：确认 `getArray(index,count)` 返回 `new Object[count]`、`getResultSet` 统一抛 SerialException、`free` 置 `elements=null`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialArrayTest.testBlackBox`）覆盖：
- 主链路：`new(StubArray)` → getBaseType/getBaseTypeName → getArray 全量/切片/map 重载 → equals/hashCode/clone → free。
- 边界：`getResultSet` 四个重载均抛 SerialException；free 后 `getArray`/`getBaseType`/`getBaseTypeName` 抛 SerialException。
- 异常路径：null Array、null map 抛 SQLException。
- 用内存 `StubArray`（不连数据库）；headless，不加 `@key`。
