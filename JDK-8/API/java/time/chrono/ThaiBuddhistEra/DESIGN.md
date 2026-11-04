# java.time.chrono.ThaiBuddhistEra 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
两纪元枚举 BEFORE_BE/BE：`valueOf/values/getValue/getDisplayName`。

## 2. Java 用户如何使用
```java
ThaiBuddhistEra.BE.getValue();  // 1
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 值 0/1；displayName 两版本差异（按侧锁定）；eraOf 身份；valueOf 非法 IAE。

### 易错细节（JDK-8 实测锁定）
- JDK-8：BE 显示名借用 `Anno Domini`/`A`；JDK-27：`BE`；BEFORE_BE FULL：`Before Christ` vs `BC`——按侧锁定。
