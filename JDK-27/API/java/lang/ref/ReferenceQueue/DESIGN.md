# java.lang.ref.ReferenceQueue — 设计方案

## 1. API 功能
引用队列：GC 清除引用后（或手动 `enqueue()` 时）把 Reference 对象排入队列供用户轮询：
- 构造：`ReferenceQueue()`（唯一 public 构造器）
- 轮询：`poll()`（非阻塞，空返回 null）
- 阻塞：`remove()`（无限等待）、`remove(long timeout)`（timeout>0 限时、=0 无限；可被中断，抛 `InterruptedException`）

## 2. Java 用户如何使用
资源清理器模式：注册 WeakReference，循环 remove 拿到失效引用做清理：
```java
ReferenceQueue<Object> q = new ReferenceQueue<>();
new WeakReference<>(resource, q);
Reference<? extends Object> r;
while ((r = q.poll()) != null) { cleanup(r); }
Reference<? extends Object> blocked = q.remove(100); // 100ms 内等待
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/ref/ReferenceQueue.java`：内部 `NULL_QUEUE`/`ENQUEUED` 哨兵，`remove` 经 `lock.wait(timeout)` 实现并响应中断。
- JDK `test/jdk/java/lang/ref/EarlyTimeout.java`：验证 `remove(timeout)` 超时返回 null 不早退；`EnqueuePollRace.java` 验证 enqueue/poll 竞争下 poll 恰好取回一次。
- Netty `io.netty.util.internal.ObjectCleaner`（`common/src/main/java/io/netty/util/internal/ObjectCleaner.java`）用 ReferenceQueue 跟踪失效的清理任务。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证具体类、public 构造器、`remove/remove(long)` 声明 `InterruptedException`、`poll` 返回 `Reference`。
黑盒（显式 enqueue，不依赖 GC；实现为单链表头进头出，出队顺序规范未规定，故只断言集合语义）：
- 空队列 `poll()`/`remove(10)`/`remove(1)` 返回 null；
- 单引用 enqueue→poll 同一对象→再 poll null；
- 三个引用入队后恰好各出队一次、且再 poll 为 null；
- 队列已有元素时 `remove()`/`remove(0)` 立即返回该元素（不阻塞）；
- 预置中断标志后 `remove(60000)` 立即抛 `InterruptedException`；
- 已出队引用再次 `enqueue()` 返回 false。
对应 `ReferenceQueueTest.java` 的 `testBlackBox()`。
