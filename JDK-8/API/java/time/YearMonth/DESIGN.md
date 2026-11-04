# java.time.YearMonth 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
值类 年-月：`of/parse/from`、`getYear/getMonthValue/getMonth`、`plus/minus/with`、`lengthOfMonth/lengthOfYear/isLeapYear`、`isValidDay(int)`、`atDay`、`compareTo/equals/hashCode/toString`。

## 2. Java 用户如何使用
```java
YearMonth ym = YearMonth.of(2024, 2);
ym.lengthOfMonth();  // 29
ym.plusMonths(1);    // 2024-03
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 12 个月 length 矩阵（2023/2024 两行）；plusMonths 年进位与月末钳制（1月+1月）；of(2024,13) DTE message **带** `valid values 1 - 12` 后缀（与 Month.of 对照）；MIN/MAX 边界。

### 易错细节（JDK-8 实测锁定）
- `YearMonth.of(13, ...)` message: `Invalid value for MonthOfYear: 13 (valid values 1 - 12)`——带后缀。
- 1 月 31 日不存在于 YearMonth，但 `isValidDay(31)` 二月平年 false、闰年 true。
