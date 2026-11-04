# java.util.concurrent.locks.AbstractOwnableSynchronizer 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 抽象类 `AbstractOwnableSynchronizer implements Serializable`；protected 构造器；protected final `setExclusiveOwnerThread(Thread)` / `getExclusiveOwnerThread()`；自身无其他公开成员。

## 2. Java 用户如何使用 + 开源用法调研
- 由 `AbstractQueuedSynchronizer`/`AbstractQueuedLongSynchronizer` 继承，记录独占持有线程（ReentrantLock 的 owner 查询即来自它）。
- JDK 源码 `locks/AbstractOwnableSynchronizer.java`：仅两方法+null 初值。
- JDK 源码 `locks/ReentrantLock.java`：`getOwner()` 即 `sync.getExclusiveOwnerThread()`。
- 参考来源：Oracle Java SE 8 javadoc。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`AbstractOwnableSynchronizerTest.java`（最小子类暴露 protected 访问器）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract、Serializable、extends Object、protected 构造器、两方法 protected final | testWhiteBox |
| P0 | 初始 owner null；set/get；set(null) 清空；可存任意线程引用 | testAOS / testOwnerLifecycle |
| P1 | **跨线程设置合法**：子线程 set 自身，主线程读到该线程（latch 确定性） | testCrossThreadSetAndGet |

- 易错细节：两方法是 protected final，只能经子类暴露后访问；不做任何并发同步（字段为 volatile 级别的可见性由实现保证）。
- 断言数：15。
