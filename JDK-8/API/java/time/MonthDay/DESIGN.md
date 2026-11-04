# java.time.MonthDay 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
值类 月-日：`of(int,int)`、`of(Month,int)`、`parse`、`from`、`atYear(int)`、`getMonthValue/getMonth/getDayOfMonth`、`isValidYear(int)`、`with(Month|MonthDay field)`、`compareTo/equals/hashCode/toString`。

## 2. Java 用户如何使用
```java
MonthDay md = MonthDay.of(2, 29);
md.isValidYear(2023);  // false
md.atYear(2024);       // 2024-02-29
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 2/29 在平年 atYear 抛 DTE；isValidYear 闰平年矩阵；with(Month) 保留 day 的钳制；toString `--02-29`；of(4,31) 抛 `Illegal value for DayOfMonth field, valid values 1 - 30`。

### 易错细节（JDK-8 实测锁定）
- `MonthDay.of(2, 29)` 本身合法，仅在 `atYear(2023)` 时非法。
- toString 前缀是 `--`（ISO 扩展格式）。
