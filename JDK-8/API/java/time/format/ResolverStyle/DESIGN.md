# java.time.format.ResolverStyle 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
枚举 STRICT/SMART/LENIENT：控制解析阶段字段冲突的处置。

## 2. Java 用户如何使用
```java
DateTimeFormatter f = DateTimeFormatter.ofPattern("uuuu-MM-dd")
    .withResolverStyle(ResolverStyle.STRICT);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 同一非法日期三档行为矩阵：`2023-02-29` STRICT 抛 PDE（精确 message）、SMART 折到 02-28、LENIENT 滚到 03-01；`2024-04-31` 同构。

### 易错细节（JDK-8 实测锁定）
- SMART 是默认档。
- STRICT 需要 `uuuu` 而非 `yyyy`（era 字段），否则解析在别处先失败。
