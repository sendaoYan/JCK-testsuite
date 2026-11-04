# java.time.temporal.TemporalAccessor 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
只读接口：`isSupported(TemporalField)`、`range(TemporalField)`、`getLong(TemporalField)`、`get(TemporalField)`、`query(TemporalQuery)`。

## 2. Java 用户如何使用
```java
int y = LocalDate.of(2024,6,15).get(ChronoField.YEAR);
ValueRange r = LocalTime.NOON.range(ChronoField.HOUR_OF_DAY);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- isSupported/range/getLong/get 在三种实现上的矩阵；get 与 getLong 一致性；query(TemporalQueries.*)；不支持的 field 抛 UTE 精确 message。

### 易错细节（JDK-8 实测锁定）
- `get` 对超 int 的字段（EPOCH_DAY）抛 UTE 要求用 getLong——容易写成 ArithmeticException。
