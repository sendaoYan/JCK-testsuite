# SQLInputImpl 设计案

## 1. API 功能
`javax.sql.rowset.serial.SQLInputImpl` 是 `java.sql.SQLInput` 的实现，从属性数组按序读取 UDT 字段（JDK 源码 `SQLInputImpl.java`）。

- 构造器：`SQLInputImpl(Object[] attributes, Map<String,Class<?>> map)`（任一为 null 抛 SQLException，属性数组被 `Arrays.copyOf` 复制）。
- 读取标量：`readString/readBoolean/readByte/readShort/readInt/readLong/readFloat/readDouble/readBigDecimal/readBytes/readDate/readTime/readTimestamp`（null 元素：包装类型返回 0/false、引用类型返回 null，并置 `wasNull`）。
- 流读取：`readCharacterStream/readAsciiStream/readBinaryStream`。
- UDT/定位器读取：`readObject/readRef/readBlob/readClob/readArray/readURL/readNClob/readNString/readSQLXML/readRowId`（均为类型转换返回属性，无类型安全检查）。
- 状态：`wasNull()`（默认 false，读到 null 后为 true）。

## 2. Java 用户如何使用
```java
SQLInputImpl in = new SQLInputImpl(new Object[]{42, "abc", null}, map);
int i = in.readInt();        // 42
String s = in.readString();  // "abc"
String n = in.readString();  // null
boolean w = in.wasNull();    // true
```
组合链路：`new(attrs,map) → readInt/readString/... 按序 → wasNull → 越界读取抛 SQLException`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SQLInputImplTests.java`：验证各 readXXX 的按序读取与 null/wasNull 语义。
- JDK 实现 `SQLInputImpl.java`：确认 `getNextAttribute` 越界抛 SQLException、null 元素时 `readInt` 等返回 0 且 `wasNull=true`、`readObject` 对 Struct 且有 map 匹配时才做自定义映射。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SQLInputImplTest.testBlackBox`）覆盖：
- 主链路：混合类型数组按序读取全部 readXXX（含 Blob/Clob/URL/Array/Ref/InputStream/Reader）。
- wasNull 语义：非 null 后 false、null 后 true、默认 false、下次非 null 重置。
- 边界：null 元素 readInt 返回 0、readBoolean 返回 false；越界读取抛 SQLException。
- 异常路径：null attributes / null map 抛 SQLException。
- 用内存 StubArray/StubRef；headless，不加 `@key`。
