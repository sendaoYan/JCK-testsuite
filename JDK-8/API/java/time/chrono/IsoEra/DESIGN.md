# java.time.chrono.IsoEra 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
两纪元枚举 BCE/CE：`valueOf/values/getValue/getDisplayName`。

## 2. Java 用户如何使用
```java
IsoEra.CE.getValue();  // 1
LocalDate.of(-5,6,15).getEra();  // BCE
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 值 0/1；负年 era=BCE 且 **YEAR_OF_ERA 比绝对值大 1**（-5 年 = BCE 6）；displayName FULL/SHORT。

### 易错细节（JDK-8 实测锁定）
- `LocalDate.of(-5,...).get(YEAR_OF_ERA)` 是 6 不是 5（无 0 年）。
- CE 的 SHORT 是 `AD`、FULL 是 `Anno Domini`（两版本一致）。
