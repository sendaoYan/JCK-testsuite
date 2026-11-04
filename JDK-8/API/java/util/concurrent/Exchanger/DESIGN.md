# java.util.concurrent.Exchanger 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`Exchanger()`。
- **交换**：`exchange(V)`（阻塞到配对，返回对方对象）、`exchange(V, long, TimeUnit)`（超时抛 TimeoutException）；两法均可抛 InterruptedException。
- **契约**：`null` 是合法的交换项（javadoc：null 可作启用交换特征的哨兵，内部以 NULL_ITEM 转义实现）。

## 2. Java 用户如何使用

- 线程 A `exchange(a)` 与线程 B `exchange(b)` 配对，A 得 b、B 得 a；单侧 `exchange(x, timeout)` 无配对时超时抛 TimeoutException，**不破坏** Exchanger，后续仍可正常交换。
- 多于两方同时到达时为自由配对语义：任意两方成对，剩余一方排队等待下一个到达者。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/Exchanger.html`）。
- JSR-166 TCK `ExchangerTest.java`、OpenJDK `test/jdk/java/util/concurrent/Exchanger/`。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致。

## 4. 测试用例设计（映射到 ExchangerTest.java，PASS 28 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 两线程互换各自值（主/worker 各得对方值，latch+join 有界） | testExchanger / testBlackBox |
| P0 | 单侧 timed exchange 超时 → TimeoutException | testBlackBox |
| P0 | **超时不毒化：TimeoutException 后同一 Exchanger 仍完成完整交换** | testTimeoutDoesNotPoison |
| P0 | **exchange(x, 0, unit) 无配对 → 立即 TimeoutException** | testZeroTimeout |
| P0 | **等待中中断 → InterruptedException（daemon + interrupt + join 有界）** | testInterrupt |
| P0 | **exchange(null) 合法：对端收到 null，另一端收到非 null 值** | testNullExchange |
| P0 | **第三方到达：三方各一次 exchange，任两方成对、剩余一方排队并干净超时；两分支（main 配对 / main 排队）均精确断言，无异常逃逸** | testThirdThreadQueues |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- 配对是自由语义：并发等待者之间也可能互相配对（无法强制「第三个线程只排队不参与本次交换」），故三方用例按调度结果分支精确断言（两分支断言数相同，PASS 计数稳定）。
- 中断在 park 前置位或 park 中送达都得到 InterruptedException（确定性）。
- 主线程侧交换全部使用有界超时（≤5s），杜绝 worker 意外死亡导致的挂死。
