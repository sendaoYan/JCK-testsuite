# java.util.concurrent.ForkJoinPool$ManagedBlocker 设计案

## 1. API 功能
ForkJoinPool 托管阻塞接口。`block()` 执行阻塞、`isReleasable()` 判断是否可跳过阻塞；由 `ForkJoinPool.managedBlock` 调度。

## 2. Java 用户如何使用
在 fork/join 任务内做可能阻塞的操作时，通过 `ForkJoinPool.managedBlock` 让池补偿工作线程。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinPool.managedBlock`；`java.util.concurrent.Phaser`/`CountedCompleter` 内部使用。

## 4. 测试用例设计 + 自动化实现
场景（18 断言）：
1. 白盒：接口 + block/isReleasable 存在；返回 boolean；block 只声明 InterruptedException（受检异常面仅此一个）；isReleasable 无受检；无 default 方法。
2. 黑盒：releasable blocker 直接返回；非 releasable blocker 调 block()。
3. releasable 路径：isReleasable=true → block() 计数 0。
4. 阻塞路径：block() 恰 1 次、released=true。
5. managedBlock(null) → NPE。
6. 异常中继：InterruptedException/RuntimeException 均 **== 原对象** 中继。
7. worker 内 managedBlock 补偿路径：阻塞释放后值 99。

易错规范细节：block() 不能声明 IOException（接口签名限制）；latch 有界 ≤5s 且 finally 释放。

实现：`ManagedBlockerTest.java`（18 断言）。优先级：调用规则 > 异常中继 == > null 边界 > 补偿路径。
