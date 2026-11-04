# java.util.concurrent.atomic.DoubleAccumulator 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`DoubleAccumulator(DoubleBinaryOperator accumulatorFunction, double identity)`。
- **累加**：`accumulate(double)`（与当前值按函数合并）。
- **查询/重置**：`get()`、`reset()`（回 identity）、`getThenReset()`。
- **Number 视图**：doubleValue==get、floatValue、longValue/intValue（向零截断）、toString（Double.toString(get)）。
- **继承**：`extends Striped64 implements Serializable`。

## 2. Java 用户如何使用

- 自定义 double 归约（求和、Math::max 滚动最大）；Double::sum 是 DoubleAdder 的通用版。

## 3. 开源/源码实际用法调研

- JDK 微基准与流式统计；JCK runtime-8b `api/java_util_concurrent_atomic/DoubleAccumulator`。

## 4. 测试用例设计（映射到 DoubleAccumulatorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | identity 初值；accumulate/get 合并语义 | testAccumulator |
| P0 | Double::sum + 可精确二进制浮点（0.5/0.25/0.125）全等 | testExactBinarySums |
| P0 | 负数合并精确；getThenReset 链（第二次返回 identity） | testExactBinarySums |
| P0 | Math::max 自定义函数（identity -Inf，滚动最大，reset 回 -Inf） | testMaxAccumulator |
| P0 | Number 视图四方法 + toString | testNumberViews |

## 关键规范细节（易错点）

- 浮点全等只用 2^-n 幂；identity 参与 get() 初值但 reset 后回到 identity 而非 0。
- Math::max 的 identity 应为 NEGATIVE_INFINITY（文档建议）。

## 断言规模

PASS (23 assertions)。
