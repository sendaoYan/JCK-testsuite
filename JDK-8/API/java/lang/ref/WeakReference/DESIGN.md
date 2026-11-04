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
