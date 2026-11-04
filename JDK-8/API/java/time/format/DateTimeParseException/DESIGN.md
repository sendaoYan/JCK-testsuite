# java.time.format.DateTimeParseException 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
DPE：`(String,String,int)`、`(String,String,int,Throwable)`，`getParsedString/getErrorIndex`，是 DateTimeException 子类。

## 2. Java 用户如何使用
```java
try { LocalDate.parse("2023-02-29"); }
catch (DateTimeParseException e) { e.getErrorIndex(); }
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 三参构造的 parsedString/errorIndex 直读；cause 链；`instanceof DateTimeException`；继承 RuntimeException。

### 易错细节（JDK-8 实测锁定）
- errorIndex 是发生错误的字符下标（从 0 起）。
- toString 形如 `java.time.format.DateTimeParseException: ...`。
