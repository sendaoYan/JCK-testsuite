# java.util.concurrent.locks.AbstractOwnableSynchronizer 测试方案

## 1. API 功能（按方法分组）
- 抽象类：`AbstractOwnableSynchronizer`（`implements Serializable`，构造器为 `protected`）。
- 持有者管理：`protected final void setExclusiveOwnerThread(Thread)`、`protected final Thread getExclusiveOwnerThread()`。
- 功能定位：独占锁的「拥有者线程」记录基类，被 AQS/AQLS 继承用于实现 `getOwner()`/`isHeldExclusively()` 等。

## 2. Java 用户如何使用
- 不直接使用：通常是实现自定义同步器时的父类（如 `AbstractQueuedSynchronizer` 继承它）。
- 子类把「当前持有独占锁的线程」写入 `exclusiveOwnerThread`；`getOwner()` 依赖它返回持有者。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/AbstractQueuedSynchronizer.java`：`extends AbstractOwnableSynchronizer`，其 `getExclusiveOwnerThread()` 用于 `getOwner()`、`isHeldExclusively()`、`toString()`。
- JDK 源码 `locks/AbstractQueuedLongSynchronizer.java`：同样继承，供 long 版同步器复用。
- JDK 源码 `locks/ReentrantLock.java`：通过内部 `Sync extends AbstractQueuedSynchronizer` 间接继承，`getOwner()` 返回 `getExclusiveOwnerThread()`。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantLock/`：覆盖 `getOwner()` 契约。

## 4. 测试用例设计 + 自动化实现
实现位置：`AbstractOwnableSynchronizerTest.java`（`testWhiteBox()` + `testBlackBox()`）。
- 白盒：`Class.forName` + 抽象类/`Serializable`/`extends Object` + `protected` 构造器 + 两个 `protected final` 方法形态。
- 黑盒（最小子类 `AOS` 暴露受保护方法）：
  - 初始 `getExclusiveOwnerThread() == null`；`set` 后返回同一线程引用。
  - `set(null)` 清空持有者；再次 `set(other)` 覆盖旧持有者（独占语义）。
- 本轮深化新增（append-only，断言 12 → 13）：
  - `testCrossThreadSetAndGet`：子线程 `setExclusiveOwnerThread(自身)` + latch，主线程 `getExclusiveOwnerThread()` 读到该线程引用（跨线程可见性）。

- 健壮性：单线程确定性、无 sleep、无全局副作用、headless-safe。
