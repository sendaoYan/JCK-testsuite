# java.lang.ref.SoftReference 测试设计案（JDK-27）

> 覆盖目标：完整契约。两个构造器、get 保留引用（软引用仅在内存压力下清除，本测试不施压）、clear、null 引用/队列语义。

## 1. API 功能

- `SoftReference(T)`、`SoftReference(T, ReferenceQueue)`；继承 get/clear/enqueue。

## 2. 用户如何使用

- 软引用比弱引用更抗回收，适合做内存敏感缓存。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/SoftReference.html）。
- JCK runtime-8b `tests/api/java_lang/ref/SoftReference/`。

## 4. 测试用例设计（映射到 SoftReferenceTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器/get/clear/null 语义 | testSoftReference |

## 易错规范细节（已锁入断言）

- `new SoftReference<>(null)`/`(null,q)`/`(o,null)` 均不抛 NPE；无队列 enqueue 返回 false。

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- 主确定性契约：**强引用在场时 get() 恒返回同一实例**——两次 System.gc() 后依然（6 样本矩阵：String/Integer/long[]/byte[]/StringBuilder/Object）。
- SoftReference 的清除由收集器**酌情**决定（通常仅内存压力下）：裸 System.gc() 循环（≤10 次×≤10ms）后仍存活的跳过路径已实测发生（本机双侧均走 NOTE 分支）——按「确定性优先」只做尽力、超时跳过记录。
- 入队语义：手动 enqueue true 一次/二次 false/poll 返回自身/poll 后 enqueue false；**enqueue() 清除 referent**（双侧实测）；已 clear 的引用仍可入队；null referent 引用可入队。
- null 组合：构造(null) 允许、(null,q) 允许、(obj,null 队列) 视同未注册队列（enqueue false、get 正常）。
- Object 契约：身份 equals、hashCode 稳定、toString 形如 java.lang.ref.SoftReference@<hex>、非 Serializable、直接子类 Reference、可子类化（TrackingSoftReference 构造链计数）。
- soft vs weak 同对象对照：强可达时两者均见 referent。

### 2. 使用方式
- 27 侧追加 refersTo 矩阵：live true/他对象 false/live 时 refersTo(null) false/clear 后 refersTo(null) true/null-referent 引用 refersTo(null) true。

### 3. 参考来源
- Oracle javadoc java.lang.ref.SoftReference（"soft references are guaranteed to have been cleared before the JVM throws OutOfMemoryError"——即无压力时保证存活）；双侧实测。
- java.lang.ref.Reference.enqueue 语义（clears + enqueues，双侧一致）。

### 4. 测试用例设计 + 易错规范细节
- testReferentIdentityMatrix（6 样本×4）、testTwoRefsSameReferent、testQueueLifecycle、testClearedRefStillEnqueues、testNullReferentEnqueues、testObjectContract（12 项）、testSubclassing、testSoftVsWeakReachability、testGcClearBestEffort（≤10×10ms，超时 println 跳过）、testRefersToMatrix（8 断言）。
- 易错点：勿断言"gc 后 soft 必被清"（无内存压力时不清，双侧实测）；gc 尽力循环写法必须超时跳过而非 fail。
