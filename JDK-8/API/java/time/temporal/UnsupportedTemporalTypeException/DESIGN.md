# java.time.temporal.UnsupportedTemporalTypeException 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
RuntimeException 子类（DateTimeException 子类）：`(String)`、`(String,Throwable)`。

## 2. Java 用户如何使用
```java
try { LocalTime.NOON.get(ChronoField.DAY_OF_MONTH); }
catch (UnsupportedTemporalTypeException e) { e.getMessage(); }
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 构造/message/cause；`instanceof DateTimeException`；由 get/plus/until 在不匹配字段时抛出的精确 message 锁定。

### 易错细节（JDK-8 实测锁定）
- message 形如 `Unsupported field: DayOfMonth`（get）与 `Unsupported unit: Months`（plus）两种句式。
