# SerialException 设计案

## 1. API 功能
`javax.sql.rowset.serial.SerialException` 继承 `java.sql.SQLException`，表示 SQL 类型（BLOB/CLOB/STRUCT/ARRAY/DATALINK/JAVA_OBJECT）序列化/反序列化错误（JDK 源码 `SerialException.java`）。

- 构造器：`SerialException()`、`SerialException(String msg)`。**注意**：类本身没有 `(String, String stateCode, int errorCode)` 构造器，只有继承自 SQLException 的成员可用。
- 继承成员：`getMessage()`、`getSQLState()`（默认 null）、`getErrorCode()`（默认 0）、`initCause`/`getCause` 等 Throwable/SQLException 方法。

## 2. Java 用户如何使用
```java
SerialException e = new SerialException("boom");
String m = e.getMessage();       // "boom"
String state = e.getSQLState();  // null
int code = e.getErrorCode();     // 0
```
组合链路：`new(String) → getMessage/getSQLState/getErrorCode → initCause/getCause`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/sql/testng/test/rowset/serial/SerialExceptionTests.java`：验证构造器与继承行为。
- JDK 实现 `SerialException.java`：确认仅两个构造器，`SerialException(String)` 调 `super(msg)`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SerialExceptionTest.testBlackBox`）覆盖：
- 主链路：无参构造（message/SQLState null、errorCode 0）与 String 构造（message 保留、SQLState null、errorCode 0）。
- 继承语义：`instanceof SQLException`；`initCause` 后 `getCause` 反映原因。
- 确定性、无副作用；headless，不加 `@key`。
