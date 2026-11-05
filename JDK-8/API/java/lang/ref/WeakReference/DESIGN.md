# java.lang.ref.WeakReference 测试设计案（JDK-8）

> 覆盖目标：完整契约。两个构造器（无队列/有队列）、get 保留引用、clear 幂等、null 引用/队列语义、有界 GC 回收。

## 1. API 功能

- `WeakReference(T)`、`WeakReference(T, ReferenceQueue)`；继承 get/clear/enqueue/isEnqueued。

## 2. 用户如何使用

- 弱引用在 GC 时被回收（只要无强引用）；用队列监听回收事件。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/WeakReference.html）。
- JCK runtime-8b `tests/api/java_lang/ref/WeakReference/`。

## 4. 测试用例设计（映射到 WeakReferenceTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器 + null 引用/队列 | testConstructors |
| P0 | clear 幂等 | testClear |
| P1 | 有界 GC 回收 | testGC |

## 易错规范细节（已锁入断言）

- `new WeakReference<>(null)`、`new WeakReference<>(null,q)`、`new WeakReference<>(o,null)` 均不抛 NPE（null 队列映射为 ReferenceQueue.NULL）。

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- 身份稳定矩阵：强可达期间 get() 恒同一实例（6 样本：String/Integer/int[]/Object/StringBuilder/BigInteger，跨 System.gc()）；StrongRetentionAcrossGc 三轮 gc 存活。
- clear 语义矩阵：clear→null、二次 clear 幂等、无队列 clear 后 enqueue false、**有队列 clear 后 enqueue true**、**enqueue() 清除 referent（双侧实测一致）**。
- 入队生命周期：一次 true/二次 false/isEnqueued 转移/poll 返回自身/poll 后 enqueue false/队列清空。
- null 组合：(null)/(null,q)/(obj,null 队列)；null referent + 队列可入队。
- Object 契约：身份 equals、hashCode 稳定、toString 形如 java.lang.ref.WeakReference@<hex>、非 Serializable、直接子类 Reference、可子类化（TrackingWeakReference）。
- 泛型变体：WeakReference<String>→ReferenceQueue<Object>、WeakReference<BigInteger>→ReferenceQueue<Number>。
- **GC 入队有界尽力（招牌）**：弃强引用后 ≤10 次 System.gc()×≤10ms，poll 到该引用即断言「入队时引用已被清除（get null）」且恰消费一次；10 轮未回收则 println 跳过（绝不断言失败）。

### 2. 使用方式
- 8 侧无 refersTo（9+ API，javap 确认），矩阵仅 27 侧。

### 3. 参考来源
- Oracle javadoc java.lang.ref.WeakReference / Reference.enqueue（clears + enqueues）；双侧实测（含 enqueue-clears-referent 探针）。
- jtreg 回归 test/jdk/java/lang/ref：有界 gc 重试范式。

### 4. 测试用例设计 + 易错规范细节
- testReferentIdentityMatrix（6×5）、testClearSemanticsMatrix、testQueueLifecycle（8）、testTwoRefsSameReferent、testObjectContract（13）、testSubclassing、testNullCombinationsExtra、testStrongRetentionAcrossGc（3 轮 gc 存活——确定性）、testGcEnqueueBestEffort（≤10×10ms 尽力）、testGenericVariance。
- 易错点：GC 尽力循环内 Thread.sleep(10) 是 API 语义等待预算的一部分（简报许可的 ≤10 次×≤10ms）；入队断言只做 poll 身份，不做顺序。
