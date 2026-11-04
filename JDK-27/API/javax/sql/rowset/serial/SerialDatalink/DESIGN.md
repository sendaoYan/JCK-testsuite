# SerialDatalink 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialDatalink` 是 SQL DATALINK 值（`java.net.URL`）的离线可序列化实现（JDK 源码 `SerialDatalink.java`）。

- 构造器：`SerialDatalink(URL)`（null 抛 SerialException）。
- 读取：`getDatalink()`——每次返回一个基于原 URL 字符串新建的 URL 副本，非同一引用。
- 对象语义：`equals`（比较 URL）、`hashCode`（`31 + url.hashCode()`）、`clone`（浅拷贝，共享 URL）。

## 2. Java 用户如何使用
```java
URL u = new URL("https://example.com/");
SerialDatalink dl = new SerialDatalink(u);
URL copy = dl.getDatalink();          // 与 u 相等，但不是同一实例
SerialDatalink dl2 = (SerialDatalink) dl.clone();
boolean same = dl.equals(dl2);        // true
```
组合链路：`new(URL) → getDatalink → equals/hashCode → clone`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialDataLinkTests.java`：验证 null URL 抛 SerialException、`getDatalink` 返回与原 URL 相等的副本、`clone` 相等、不同 URL 不相等、序列化往返相等。
- JDK 实现 `SerialDatalink.java`：确认 `getDatalink` 用 `new URL(url.toString())` 返回新对象、`hashCode = 31 + url.hashCode()`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialDatalinkTest.testBlackBox`）覆盖：
- 主链路：`new(URL)` → getDatalink 值相等且非同一引用 → equals/hashCode → clone → 不同 URL 不相等。
- 边界：equals 对 null、非 SerialDatalink 返回 false。
- 异常路径：null URL 抛 SerialException。
- 固定 URL（无网络访问）；headless，不加 `@key`。
