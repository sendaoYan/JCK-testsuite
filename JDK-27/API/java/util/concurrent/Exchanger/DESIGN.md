# java.util.concurrent.Exchanger 设计案

## 1. API 功能
两线程交换对象的同步点。构造 `()`。`exchange(V)` 阻塞直到配对的另一线程调用 exchange，返回对方对象（null 是合法交换项，javadoc 明示可作启用交换特征的哨兵）；`exchange(V, timeout, unit)` 超时抛 `TimeoutException`（不破坏 Exchanger）。

## 2. Java 用户如何使用
- 构造：`new Exchanger<String>()`。
- 链路：线程 A `exchange(a)` 与线程 B `exchange(b)` 配对，A 得 b、B 得 a；单侧 `exchange(x, timeout)` 无配对时超时抛 TimeoutException；超时后同一实例仍可正常交换；多于两方并发到达为自由配对，剩余方排队。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/Exchanger/`、`tck/ExchangerTest.java` 覆盖双线程交换与超时。
- 场景：流水线两阶段缓冲交换（生产者/消费者交接槽位）的经典同步原语。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致。

## 4. 测试用例设计 + 自动化实现（PASS 28 断言）
场景：
1. 白盒：类 + exchange(V)/exchange(V, long, TimeUnit)。
2. 黑盒：主线程 + worker 线程互相交换对象（AtomicReference + CountDownLatch 确定性同步）；无配对 timed exchange 抛 TimeoutException。
3. 契约深化（新增）：超时不毒化（TimeoutException 后同一实例完成完整交换）；exchange(x, 0, unit) 立即超时；等待中中断 → InterruptedException（daemon + interrupt + join 有界）；exchange(null) 合法且对端精确收到 null；三方各一次 exchange 的自由配对语义（main 配对或排队两分支均精确断言，断言数相同）。

实现：`ExchangerTest.java`。线程纪律：新线程 daemon、等待 ≤5s 有界、join(ms) 有界、主线程侧交换全部有界超时、无 sleep 轮询。
