# java.util.concurrent.atomic.LongAccumulator 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`LongAccumulator(LongBinaryOperator, long identity)`。
- **累加/查询/重置**：`accumulate(long)`、`get()`、`reset()`、`getThenReset()`。
- **Number 视图**：longValue==get、intValue、floatValue、doubleValue、toString（Long.toString(get)）。
- **继承**：`extends Striped64 implements Serializable`。

## 2. Java 用户如何使用

- 自定义 long 归约：`Long::sum`（求和）、乘法（identity 1）、`Math::max`（滚动最大，identity MIN_VALUE）。

## 3. 开源/源码实际用法调研

- JDK 微基准与流计数；JCK runtime-8b `api/java_util_concurrent_atomic/LongAccumulator`。

## 4. 测试用例设计（映射到 LongAccumulatorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 乘法累加器（identity 1）accumulate/reset/getThenReset | testAccumulator |
| P0 | Long::sum identity 0 + 精确累计 + toString | testSumAccumulator |
| P0 | **64 位环绕契约：accumulate(MAX)×2 → get()==-2、MIN+(-1)→MAX，均不抛 ArithmeticException**（JDK-8 实测；javadoc 未定义精确和语义） | testOverflowWrapContract |
| P0 | Math::max 自定义函数全分支（含相等值、reset 回 identity、getThenReset） | testMaxAccumulator |
| P1 | Number 视图四方法 | testNumberViews |

## 关键规范细节（易错点）

- 经典契约点：LongAccumulator 的 get()/longValue() 是**补码环绕**语义，不存在 Math.addExact 式的 ArithmeticException——与 LongAdder.sum() 一致；doubleValue 返回环绕后 long 的 double 转换。
- reset 回 identity（不是 0）。

## 断言规模

PASS (29 assertions)。
