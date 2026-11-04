# java.time.DateTimeException 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
RuntimeException 子类：`(String)`、`(String,Throwable)`；`getMessage/getCause` 直通。

## 2. Java 用户如何使用
```java
try { LocalDate.of(2023, 2, 29); } catch (DateTimeException e) {
    e.getMessage();  // Invalid date 'FEBRUARY 29'
}
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 断言携带精确 message 的构造与读取、cause 链、继承关系（RuntimeException、非受检）。
- 与 DateTimeParseException（其子类，位于 format 包）、ZoneRulesException（其子类，位于 zone 包）区分。

### 易错细节（JDK-8 实测锁定）
- `DateTimeParseException instanceof DateTimeException` 为 true（容易答反）。
- JDK-8 的 NPE message 是裸参数名（如 `instant`），与 JDK-27 的 helpful message 不同——按侧锁定。
