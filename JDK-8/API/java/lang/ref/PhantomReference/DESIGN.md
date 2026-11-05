# java.lang.ref.PhantomReference 测试设计案（JDK-8）

> 覆盖目标：完整契约。构造器要求队列、get() 恒 null（即使引用存活）、null 引用/队列语义、enqueue/isEnqueued、队列 poll。

## 1. API 功能

- `PhantomReference(T, ReferenceQueue)`（必须带队列）；继承 clear/enqueue/isEnqueued。

## 2. 用户如何使用

- 幽灵引用不保留引用（get 恒 null），仅在对象回收后入队，用于回收后清理资源。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/ref/PhantomReference.html）。
- JCK runtime-8b `tests/api/java_lang/ref/PhantomReference/`。

## 4. 测试用例设计（映射到 PhantomReferenceTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | get 恒 null + enqueue/poll | testPhantomReference |

## 易错规范细节（已锁入断言）

- `get()` 恒 null（区别于 Weak/Soft）；`new PhantomReference<>(null,q)`/`(o,null)` 不抛 NPE。

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- **get() 恒 null 招牌契约**：8 种 referent（String/Integer/long[]/byte[]/StringBuilder/Object/空串）× 构造后/重复调用/System.gc() 后/clear 后/clear+gc 后全部 null——即使 referent 强可达。
- **new PhantomReference(obj, null) 不抛 NPE**（双侧实测；任务简报预期 NPE 有误，已按实测锁定）：构造允许、get null、isEnqueued false、enqueue false——「完全无用」的引用（javadoc 明示此语义）。
- null referent 生命周期：get null、enqueue true、poll 返回自身（双侧实测）。
- 手动 enqueue 状态机：true 一次/二次 false/poll 自身/poll 后 false/队列清空；**enqueue 后 get 仍 null**；clear 后仍可 enqueue。
- 同一对象多个 phantom：各自独立注册（含同队列两个、跨队列），逐个 drain 集合语义。
- weak+phantom 同对象对照：weak.get==o 且 phantom.get==null 并存；weak.clear 不影响 phantom。
- Object 契约：身份 equals、hashCode 稳定、toString 形如 java.lang.ref.PhantomReference@<hex>、非 Serializable、直接子类 Reference、可子类化（TrackingPhantom）。
- GC 入队尽力：弃强引用 ≤10 次 gc×≤10ms → poll 到该 phantom 即断言「入队后 get 仍 null」且恰消费一次；超时跳过。

### 2. 使用方式
- 8 侧无 refersTo（9+ API），矩阵仅 27 侧文件。

### 3. 参考来源
- Oracle javadoc java.lang.ref.PhantomReference："It is possible to create a phantom reference with a null queue. Such a reference is completely useless: Its get method will always return null and, since it does not have a queue, it will never be enqueued."（null 队列允许的权威依据）；双侧实测。
- JLS 12.6 phantom-reachable 语义（enqueued after the collector learns referent is unreachable & finalizable）。

### 4. 测试用例设计 + 易错规范细节
- testGetAlwaysNullMatrix（8 样本×5）、testNullQueueAllowed、testNullReferentLifecycle、testEnqueueStateMachine（9）、testEnqueueAfterClear、testMultiplePhantomsSameReferent（集合语义）、testMixedWeakAndPhantom、testObjectContract（14）、testSubclassing、testGcEnqueueBestEffort。
- 易错点：① null 队列构造不抛 NPE（勿按直觉断 NPE）；② 手动 enqueue 顺序未规定；③ enqueue 清 referent 对 phantom 的 get 不可观测（恒 null），断言只能写「仍 null」。
