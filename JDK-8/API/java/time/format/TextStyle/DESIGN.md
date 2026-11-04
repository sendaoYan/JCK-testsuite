# java.time.format.TextStyle 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举 FULL/SHORT/NARROW 与 STANDALONE 变体：`asStandalone()`、`isStandalone()`、`values/valueOf`。

## 2. Java 用户如何使用
```java
Month.FEBRUARY.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 三基本档 × standalone 与否的全矩阵（Month/DayOfWeek）；asStandalone 映射。

### 易错细节（JDK-8 实测锁定）
- JDK-8 SHORT_STANDALONE 英文是 `2`（数字），JDK-27 是 `Feb`——按侧锁定。
