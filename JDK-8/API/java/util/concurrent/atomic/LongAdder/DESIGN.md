# java.util.concurrent.atomic.LongAdder 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`LongAdder()`（初值 0）。
- **累加**：`add(long)`、`increment()`、`decrement()`。
- **汇总**：`sum()`、`sumThenReset()`、`reset()`。
- **Number 视图**：`longValue`/`intValue`/`floatValue`/`doubleValue`、`toString`。
- **继承/契约**：`extends Striped64` + `Serializable`。

## 2. Java 用户如何使用

- 高并发累加器，`add()` 分摊到多个 cell，`sum()` 汇总，吞吐优于 AtomicLong。
- `sumThenReset()` 一次性取和并清零；`reset()` 清零。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/atomic/LongAdder.html`）。
- OpenJDK `test/jdk/java/util/concurrent/atomic/`。
- JCK runtime-8b `api/java_util_concurrent_atomic/LongAdder`。

## 4. 测试用例设计（映射到 LongAdderTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | add/increment/decrement/sum 累加语义 | testSingleThread |
| P0 | reset/sumThenReset 清零语义 | testSingleThread |
| P0 | toString = Long.toString(sum) | testSingleThread |
| P0 | Number 视图四方法 | testNumberViews |
| P0 | 多线程 8×100k add(1L) 精确和 | testConcurrent |
| P1 | 序列化 round-trip 保和 | testSerialization |
| P1 | 白盒：extends Striped64 / Serializable / 方法 | testWhiteBox |

## 关键规范细节

- `sum()` 无 happens-before 语义（并发下可能略滞后），但单线程与 join 后精确。
- `sumThenReset()` 返回旧和并清零；`reset()` 仅清零不返回。

## 5. 契约深化追加（本轮）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 负数 add / decrement 越零 / sumThenReset 二次调用返回 0 / toString 负值 | testNegativeAndReset |
| P0 | sum 跨 Long.MAX_VALUE 补码环绕（无异常） | testNegativeAndReset |
| P1 | 两 daemon 线程各 1000 次 add/increment，join(10s) 有界后 sum==2000 | testTwoThreadBounded |

断言规模：PASS (32 assertions)。
