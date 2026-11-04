# java.util.concurrent.atomic.AtomicStampedReference 测试设计

## 1. API 功能
- 构造器：`AtomicStampedReference(V, int)`。
- 查询：`getReference()`、`getStamp()`、`get(int[])`。
- 原子更新：`set(V, int)`、`compareAndSet(ref, newRef, stamp, newStamp)`、
  `weakCompareAndSet(ref, newRef, stamp, newStamp)`、`attemptStamp(expectedRef, newStamp)`。
- 引用比较使用 `==`；stamp 为 int 版本号。非 `Serializable`。

## 2. Java 用户如何使用
- 引用 + 整数版本号，用于 ABA 防护：
  `AtomicStampedReference<Node> r = new AtomicStampedReference<>(head, 0);`
- CAS 同时改引用与版本：`r.compareAndSet(old, newNode, oldStamp, oldStamp + 1)`。
- 仅改版本：`r.attemptStamp(node, newStamp)`（引用不变）。
- 组合链路：`set → getStamp → compareAndSet → attemptStamp`。

## 3. 开源/源码实际用法调研
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/Serial.java` 覆盖原子类序列化（同族参照）。
- 开源（ConcurrentLinkedQueue 等无锁结构思路）用 stamped 引用实现 ABA-safe 无锁栈/队列。
- JDK 源码 `java/util/concurrent/atomic/AtomicStampedReference.java` javadoc 描述 ABA 防护用途。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicStampedReferenceTest.testBlackBox()`）：
  - 构造引用/版本、getReference/getStamp/get(int[])。
  - set 同时改引用与版本。
  - compareAndSet 成功；引用不匹配、版本不匹配分别失败且状态不变。
  - weakCompareAndSet 成功/失败。
  - attemptStamp 成功（引用匹配只改版本）、失败（引用不匹配版本不变）。
