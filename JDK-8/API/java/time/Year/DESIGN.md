# java.time.Year 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
值类 年：`of/isLeap/parse/from/now`、`getValue`、`atMonth(Month|int)/atDay/atMonthDay`、`plus/minus/with`、`isValidMonthDay`、`length/isLeap`、`compareTo/equals/hashCode/toString/format/query`。

## 2. Java 用户如何使用
```java
Year y = Year.of(2024);
y.isLeap();          // true
y.atMonthDay(MonthDay.of(2, 29)); // 2024-02-29
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 闰年规则（4/100/400）；MIN/MAX 边界 1900/9999 之外的 of 越界 DTE；atDay 365/366 越界；parse `+2024` 在 JDK-8 抛 DPE（JDK-27 允许）——按侧锁定；plusYears 越界 message。

### 易错细节（JDK-8 实测锁定）
- `Year.parse("+2024")` JDK-8 DPE `Text cannot be parsed at index 0`，JDK-27 OK——版本差异。
- `Year.of(9999).plusYears(1)` 抛 DTE，message 提 `Year`。
