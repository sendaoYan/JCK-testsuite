# java.lang.ref.ReferenceQueue 测试设计案（JDK-27）

> 覆盖目标：完整契约。poll 空→null、remove(1) 空→null、remove(-1)/MIN_VALUE→IAE、enqueue→poll/remove、
> remove() 已入队引用、remove(0) 阻塞语义（不在断言内）。

## 1. API 功能

- `poll()` 非阻塞取队首；`remove()`/`remove(long)` 阻塞/超时取引用。

## 2. 用户如何使用

- 配合 Reference 的 enqueue 观察回收事件；`poll()` 非阻塞，`remove(timeout)` 阻塞至超时。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/ReferenceQueue.html）。
- JCK runtime-8b `tests/api/java_lang/ref/ReferenceQueue/`。

## 4. 测试用例设计（映射到 ReferenceQueueTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 空队列 poll/remove(1)/remove(-1) | testEmptyQueue |
| P0 | enqueue→poll/remove 往返 | testPollAndRemove |

## 易错规范细节（已锁入断言）

- `remove(-1)`/`remove(MIN_VALUE)` → IllegalArgumentException（JDK-8）。

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- 空队列语义：poll 永不阻塞恒 null（1000 次循环验证）、remove(正 timeout) 超时返回 null。
- **remove(负数) → IllegalArgumentException，精确消息 "Negative timeout value"**（jdk8u372 与 jdk-27.0.1 双侧逐字一致；注意与常见猜测 "timeout value is negative" 不同，任务简报预期即为该猜测，实测已纠正锁定）。
- remove() / remove(0) 为无限等待——**测试绝不调用**（挂死风险）。
- 手动 enqueue 的**相对顺序未规定**（实测双侧均非 FIFO：三连入队首 poll 得第二个），只断言集合语义：N 次入队恰可 drain N 个且均为注册成员。
- 清除过的引用、null referent 引用均可手动入队并 poll。
- 跨线程：阻塞中的 remove() 被另一线程入队后立即返回该引用（确定性：先入队则 remove 直接返回，后入队则阻塞唤醒，两种时序同结果）；阻塞中的 remove() 被中断 → InterruptedException（有界 latch 验证）。
- **预置中断标志后调 remove(1)：双侧均不抛 InterruptedException 且标志保留**（quirk 锁定记录，不做断言方向假设）。
- 泛型变体：ReferenceQueue<? super T> 协变注册（String→Object 队列、BigInteger→Number 队列、Phantom→Object 队列）；poll 返回 Reference<? extends T>。
- 队列可复用：drain 后再注册再 drain 循环三轮；单引用一次性 enqueue（二次 false、poll 后 false）。

### 2. 使用方式
- 跨线程用例全部遵守 jtreg MainThreadGroup 约束：worker 为 daemon 且 try/catch(Throwable) 内吞、CountDownLatch(10s) 有界 await、join(10000) 收尾。

### 3. 参考来源
- Oracle javadoc java.lang.ref.ReferenceQueue（remove(long) timeout 语义：0=无限、负=IAE、正=毫秒上限；poll 非阻塞）。
- 双侧实测探针（/tmp/lcl-probe8/P8.java、/tmp/lcl-probe27/P27.java）：消息文本、非 FIFO、中断行为。

### 4. 测试用例设计 + 易错规范细节
- testNegativeTimeoutExactMessage（-1/-2/-1000/MIN_INT+1/MIN_LONG 五点消息全等）、testDrainSetSemantics（5 引合 drain 集合断言）、testCrossThreadHandoff、testInterruptedRemove（WAITING 状态有界自旋后 interrupt，确保真正阻塞中被中断）、testGenericQueueVariance、testQueueReuseAcrossCycles、testEnqueueOncePerReference。
- 易错点：① 千万别调 remove()/remove(0)；② 中断测试必须先确认线程已 WAITING（用 getState 有界自旋，非 sleep）；③ BigInteger 替代 Integer 作 Number referent 避免 27 侧 value-based 告警。
