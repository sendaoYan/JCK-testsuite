# java.time.DateTimeException 测试方案

## 1. API 功能（按方法分组）

- 构造器：`DateTimeException(String message)`、`DateTimeException(String message, Throwable cause)`。
- 继承自 `RuntimeException`/`Throwable`：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 构造：`throw new DateTimeException("Invalid value for HourOfDay: 24");`
- 典型调用：`catch (DateTimeException e) { e.getMessage(); e.getCause(); }`。
- 组合链路：构造带 cause 的异常 → `getMessage/getCause` 传播底层原因。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/time/DateTimeException.java`：两个构造器直接委托父类。
- JDK 源码 `java.time` 各字段（如 `LocalDate`/`LocalTime`/`ZoneId`）校验失败时抛 `DateTimeException`。
- JDK 测试 `test/jdk/java/time/*.java` 大量 `assertThrows(DateTimeException.class, ...)`。

## 4. 测试用例设计 + 自动化实现

实现位置：`DateTimeExceptionTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：继承 `RuntimeException`、两个构造器签名存在。
- 黑盒：单参 message、双参 message+cause、默认无 cause、`getStackTrace` 非空、仍为 `RuntimeException`。
- 健壮性：纯对象构造，headless-safe，不加 `@key`。
