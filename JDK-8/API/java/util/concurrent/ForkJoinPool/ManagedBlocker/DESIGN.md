# java.util.concurrent.ForkJoinPool.ManagedBlocker 设计案（JDK-8）

## 1. API 功能
接口，两方法：`boolean block() throws InterruptedException`（**受检异常面只有 InterruptedException，无法声明 IOException**）与 `boolean isReleasable()`（无受检异常）；无 default 方法。`ForkJoinPool.managedBlock(blocker)` 在 FJT 内阻塞时补偿线程，避免池饥饿。

## 2. Java 用户如何使用
- 实现 blocker 包装锁/latch/IO：`isReleasable()` 返回 true 则 block() 永不被调；否则 block() 被调用直至返回 true 或抛异常。
- worker 内调用 `managedBlock` 会尝试补偿（可能临时增加并行度）。
- `managedBlock(null)` → NPE；block() 抛出的异常原样中继给调用者（== 保持）。

## 3. 开源代码实际用法调研
- `java.util.concurrent.locks.AbstractQueuedSynchronizer`、`Phaser`、`ReentrantLock` 的 await 内部经 managedBlock 桥接。
- JDK 自带测试：`tck/ForkJoinPoolTest.java` 的 ManagedBlock 场景与 `ForkJoinPool8Test`。

## 4. 测试用例设计 + 自动化实现
场景（15 断言）：
1. 白盒：接口/block 返回 boolean 且只声明 InterruptedException/isReleasable 无受检异常/无 default 方法。
2. releasable 路径：isReleasable=true → block() 调用计数 0。
3. 阻塞路径：预开 latch、block() 恰好调 1 次、完成后 released=true。
4. managedBlock(null) → NPE。
5. 异常中继：InterruptedException == 原对象；RuntimeException == 原对象（== 断言，同线程）。
6. worker 内 managedBlock：阻塞后释放 → 值 99（补偿路径 exercised）。

易错规范细节：block() 不能声明 IOException（接口签名限制，只能测 InterruptedException + RuntimeException）；所有 latch 有界 ≤5s 且 finally 释放。

实现：`ManagedBlockerTest.java`（15 断言）。优先级：调用规则（releasable→不调 block）> 异常中继 == > null 边界 > worker 补偿路径。
