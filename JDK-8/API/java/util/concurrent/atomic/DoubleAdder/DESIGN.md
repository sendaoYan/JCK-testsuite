# java.util.concurrent.atomic.DoubleAdder 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`DoubleAdder()`（初值 0.0）。
- **累加**：`add(double)`。
- **汇总/清零**：`sum()`（当前和）、`reset()`、`sumThenReset()`（返回旧和并清零）。
- **Number 视图**：`doubleValue`==sum、`floatValue`、`longValue`/`intValue`（向零截断）、`toString`（Double.toString(sum)）。
- **继承**：`extends Striped64 implements Serializable`。

## 2. Java 用户如何使用

- 高频 double 累加（统计直方图、指标求和），sum() 时汇总 cells；并发吞吐优于 AtomicReference<Double> 的 CAS 循环。

## 3. 开源/源码实际用法调研

- JDK 微基准 `test/micro/.../atomic/`；Dropwizard metrics 等用 LongAdder（同族），DoubleAdder 用于 double 指标。
- JCK runtime-8b `api/java_util_concurrent_atomic/DoubleAdder`。

## 4. 测试用例设计（映射到 DoubleAdderTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 初始 sum 0.0；add/sum；reset | testAdder |
| P0 | **只用二进制可精确表示值（0.5/0.25/0.125）：sum 全等断言** | testExactBinarySums |
| P0 | 负数 add 精确；+0.0（非 -0.0）语义 | testExactBinarySums |
| P0 | sumThenReset 返回旧和、二次调用返回 0 | testResetSemantics |
| P0 | Number 视图四方法（longValue 向零截断 2.75→2）+ toString | testNumberViews |
| P1 | 序列化 round-trip 保和 | testSerialization |
| P1 | 双 daemon 线程各 1000×add(0.5)，join 有界后 sum==1000.0 精确 | testTwoThreadBounded |

## 关键规范细节（易错点）

- 断言不得用 0.1/0.2 等不可精确二进制值做全等（并行下合并顺序影响舍入）——只用 2^-n 幂值。
- sum() 无快照语义（并发下弱一致），单线程与 join 后精确。

## 断言规模

PASS (29 assertions)。
