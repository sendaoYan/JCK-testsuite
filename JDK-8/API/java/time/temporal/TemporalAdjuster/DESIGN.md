# java.time.temporal.TemporalAdjuster 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
函数接口：`adjustInto(Temporal)`；实现类 TemporalAdjusters 的全套静态工厂。

## 2. Java 用户如何使用
```java
LocalDate d = LocalDate.of(2024, 6, 15)
    .with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 自定义 lambda adjuster；与 `with(TemporalAdjuster)` 组合；`LocalDate.adjustInto` 语义。

### 易错细节（JDK-8 实测锁定）
- adjustInto 返回新对象，不改原对象（值类不可变）。
- null adjuster 抛 NPE `adjuster`。
