# java.time.chrono.HijrahEra 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
单纪元枚举 AH：`valueOf/values/getValue/getDisplayName`。

## 2. Java 用户如何使用
```java
HijrahEra.AH.getValue();  // 1
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 单纪元事实（values 长度 1）；getValue 1；三个 TextStyle 的 displayName；valueOf 非法枚举 IAE message。

### 易错细节（JDK-8 实测锁定）
- displayName：JDK-8 `Anno Domini`/`AD`/`A`（COMPAT 借用公历名），JDK-27 `Anno Hegirae`/`AH`/`AH`（CLDR）——按侧锁定。
- range(YEAR_OF_ERA) UTE。
