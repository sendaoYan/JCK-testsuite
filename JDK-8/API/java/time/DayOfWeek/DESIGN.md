# java.time.DayOfWeek 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举 MONDAY..SUNDAY：`of(int)`、`valueOf(String)`、`values()`、`getValue()`（ISO 1..7）、`plus/minus(long)`、`getDisplayName(TextStyle,Locale)`、`get(TemporalField)/getLong/isSupported/range/query/adjustInto`、`compareTo`。

## 2. Java 用户如何使用
```java
DayOfWeek f = DayOfWeek.of(5);        // FRIDAY
DayOfWeek m = DayOfWeek.MONDAY.plus(3); // THURSDAY
f.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- of 边界 1/7 与越界（0/8 → DTE，JDK-8 message 不带范围后缀）；plus/minus 模 7 回绕；getValue 映射；displayName FULL/SHORT/NARROW；adjustInto 把 LocalDate 的星期换掉。

### 易错细节（JDK-8 实测锁定）
- `DayOfWeek.of(0)` 的 message 是 `Invalid value for DayOfWeek: 0`，**不带** `valid values 1 - 7` 后缀（Month.of 同理，与 YearMonth.of 的带后缀 message 形成对照）。
