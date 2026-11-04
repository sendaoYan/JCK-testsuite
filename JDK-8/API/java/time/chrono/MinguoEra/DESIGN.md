# java.time.chrono/MinguoEra 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
两纪元枚举 BEFORE_ROC/ROC：`valueOf/values/getValue/getDisplayName`。

## 2. Java 用户如何使用
```java
MinguoEra.ROC.getValue();  // 1
MinguoDate.of(113,6,15).getEra(); // ROC
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 值 0/1；三档 displayName（两版本差异大，按侧锁定）；valueOf 非法 IAE；chronology.eraOf 身份同一。

### 易错细节（JDK-8 实测锁定）
- JDK-8：ROC 显示名借用 `Anno Domini`/`AD`/`A`；JDK-27：`Minguo`。BEFORE_ROC：JDK-8 `Before Christ`/`BC`/`B`，JDK-27 `B.R.O.C.`——全部按侧锁定。
