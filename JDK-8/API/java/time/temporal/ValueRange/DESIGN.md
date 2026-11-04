# java.time.temporal.ValueRange 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
值域：`of(min,max)/of(min,maxMax)/of(smalMin,largeMin,maxMax)/of(smalMin,largeMin,smalMax,largeMax)`、`isValidIntValue/isValidLongValue/isValidValue/checkValidValue/checkValidIntValue`、`getSmallestMinimum/getLargestMinimum/getMinimum/getMaximum/isFixed/equals/toString`。

## 2. Java 用户如何使用
```java
ValueRange r = ValueRange.of(1, 31);
r.checkValidValue(15, ChronoField.DAY_OF_MONTH);
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- 四种工厂的四个极值组合；checkValidValue 越界 DTE 精确 message（含字段名）；isFixed；equals/hashCode；toString。

### 易错细节（JDK-8 实测锁定）
- `ValueRange.of(3, 1, 2)`（smallestMin>largestMax）JDK-8 抛 IAE，message 与 JDK-27 略异——按侧锁定。
- checkValidIntValue 对超 int 范围值抛 DPE/DTEx 指明字段。
