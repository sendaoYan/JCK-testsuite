# SQLOutputImpl 设计案

## 1. API 功能
`javax.sql.rowset.serial.SQLOutputImpl` 是 `java.sql.SQLOutput` 的实现，把 UDT 字段按序写入调用方持有的 `Vector`（JDK 源码 `SQLOutputImpl.java`）。

- 构造器：`SQLOutputImpl(Vector<?> attributes, Map<String,?> map)`（任一为 null 抛 SQLException；Vector 是调用方持有的，可直接检查）。
- 标量写入：`writeString/Boolean/Byte/Short/Int/Long/Float/Double/BigDecimal/Bytes/Date/Time/Timestamp/NString`（包装后 `attribs.add`）。
- UDT 写入：`writeObject(SQLData)`（null 写 null，否则包装为 SerialStruct）、`writeRef/Blob/Clob/Array`（null 写 null，否则包装为 SerialRef/SerialBlob/SerialClob/SerialArray）、`writeURL`（包装为 SerialDatalink）、`writeStruct(Struct)`（包装为 SerialStruct）。
- 流写入：`writeCharacterStream/AsciiStream/BinaryStream`（实现较怪，未在用例中覆盖）。

## 2. Java 用户如何使用
```java
Vector<Object> v = new Vector<>();
SQLOutputImpl out = new SQLOutputImpl(v, map);
out.writeInt(42);
out.writeString("abc");
out.writeRef(ref);        // v 中追加 SerialRef
// 之后直接检查 v 的内容
```
组合链路：`new(v,map) → writeInt/writeString/writeRef/... → 检查 v 元素类型与值`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SQLOutputImplTests.java`：验证标量写入追加到 Vector、UDT 写入包装为对应 Serial 类型。
- JDK 实现 `SQLOutputImpl.java`：确认各 writeXXX 把值 `attribs.add(...)`，`writeObject(SQLData)` 走 SerialStruct(SQLData) 构造。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SQLOutputImplTest.testBlackBox`）覆盖：
- 主链路：写入 22 个标量+UDT 值并逐一断言 Vector 元素类型与值（writeInt/Long/String/Boolean/Double/Byte/Short/Float/BigDecimal/Bytes/Date/Time/Timestamp/NString 及 writeRef/Blob/Clob/Array/URL/Struct/Object）。
- 边界：writeObject(null) 与各 UDT writeXxx(null) 写 null。
- 异常路径：null Vector / null map 抛 SQLException。
- 用内存 StubArray/StubRef/StubStruct/HeroData；headless，不加 `@key`。
