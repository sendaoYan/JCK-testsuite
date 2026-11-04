# SerialClob 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialClob` 是 `java.sql.Clob` 的离线可序列化实现，把 CLOB 字符内容物化到内存 `char[]`（JDK 源码 `SerialClob.java`）。

- 构造器：`SerialClob(char[])`、`SerialClob(Clob)`。
- 读取：`length()`、`getSubString(long pos, int length)`（1-based）、`getCharacterStream()`、`getCharacterStream(long pos, long length)`、`getAsciiStream()`（仅 Clob 构造支持，char[] 构造抛 SerialException）。
- 搜索：`position(String, long)`、`position(Clob, long)`，找不到或 start 越界返回 -1。
- 写入：`setString(long pos, String)`、`setString(long pos, String, int offset, int length)`、`setAsciiStream`/`setCharacterStream`（仅 Clob 构造支持）。
- 修改：`truncate(long)`。
- 生命周期/对象语义：`free()`、`equals`、`hashCode`、`clone`。

## 2. Java 用户如何使用
```java
SerialClob cl = new SerialClob("hello".toCharArray());
long n = cl.length();                 // 5
String s = cl.getSubString(2, 3);     // "ell"（1-based）
long p = cl.position("ell", 1);       // 2
cl.setString(6, "XYZ");               // 追加，长度变 8
cl.truncate(3);
SerialClob copy = (SerialClob) cl.clone();
cl.free();
```
组合链路：`new(char[]) → length → getSubString → getCharacterStream → position → setString → truncate → clone/equals → free`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialClobTests.java`：覆盖 `getSubString`/`getCharacterStream`/`setString`/`truncate`/`position`/`free` 语义。
- JDK 实现 `SerialClob.java`：确认 `getSubString` 1-based、`position` 找不到返回 -1、char[] 构造下 `getAsciiStream` 抛 SerialException、`free` 置 `buf=null`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialClobTest.testBlackBox`）覆盖：
- 主链路：构造→length→getSubString(全量/1-based 切片/零长)→getCharacterStream(全量/区间)→getAsciiStream 异常→position(String/Clob/未找到/越界)→setString(追加/覆盖/offset+length)→truncate(0/3)→空数组→equals/hashCode/clone→free 后异常。
- 边界：`getSubString` pos=0 / pos>length / pos+length>length 抛 SerialException；`position` 找不到/start 越界返回 -1。
- 异常路径：char[] 构造下 `getAsciiStream` 抛 SerialException；free 后 `length`/`getSubString` 抛 SerialException。
- 确定性：固定字符串内容；headless，不加 `@key`。
