# java.time.Month 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举 JANUARY..DECEMBER：`of(int)`、`valueOf`、`values()`、`getValue()`、`plus/minus`、`minLength()/maxLength()/length(boolean)`、`firstDayOfYear(boolean)`、`firstMonthOfQuarter()`、`dayOfYear(int,boolean)`、`getDisplayName`、`isSupported/get/getLong/adjustInto`。

## 2. Java 用户如何使用
```java
Month m = Month.FEBRUARY;
m.maxLength();      // 29
Month.of(2).length(true);  // 29
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 12 个月的天数表（平/闰两列，2 月 28/29、7/8 月 31）；STANDALONE 风格；plus 回绕；越界 DTE message 无范围后缀。

### 易错细节（JDK-8 实测锁定）
- JDK-8(COMPAT locale data) `Month.FEBRUARY.getDisplayName(TextStyle.SHORT_STANDALONE, ENGLISH)` 是 `2`，JDK-27(CDR) 是 `Feb`；FULL_STANDALONE 同理 `February`——按侧锁定。
