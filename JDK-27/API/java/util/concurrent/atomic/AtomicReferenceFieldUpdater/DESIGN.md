# java.util.concurrent.atomic.AtomicReferenceFieldUpdater 测试设计

## 1. API 功能
- 工厂：`static <U,W> newUpdater(Class<U>, Class<W>, String)`。
- 抽象方法：`compareAndSet(T,V,V)`、`weakCompareAndSet(T,V,V)`、`set(T,V)`、`lazySet(T,V)`、`get(T)`。
- 默认方法：`getAndSet`、`getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 保护构造器；抽象类；非 `Serializable`。

## 2. Java 用户如何使用
- 对既有对象中的 `volatile` 引用字段做原子更新（如链表的 next 指针）：
  `AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next")`。
- 约束：字段必须 `volatile` 且与给定 `W` 类型完全一致；类型不符抛 `ClassCastException`，
  非 volatile 抛 `IllegalArgumentException`。
- 组合链路：`newUpdater → set → compareAndSet → getAndUpdate`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/sql/SQLException.java`：
  `AtomicReferenceFieldUpdater<SQLException,SQLException> nextUpdater =
  newUpdater(SQLException.class, SQLException.class, "next");` 链式异常的无锁 next 更新。
- JDK 源码 `java/io/BufferedInputStream.java` 注释：说明为何用 `Unsafe.compareAndSetReference`
  而非 `AtomicReferenceFieldUpdater`（跨包私有字段访问限制）。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java`：验证引用字段 updater。
- 开源（Guava/Netty）用其实现无锁链表/队列的指针 CAS。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicReferenceFieldUpdaterTest.testBlackBox()`）：
  - 完整字段更新操作链（set/get/compareAndSet/weakCompareAndSet/getAndSet/update/accumulate/lazySet）。
  - 两个 updater 指向不同字段相互独立。
  - 异常：非 volatile → IllegalArgumentException；原始类型字段（int）→ IllegalArgumentException（必须引用）；
    引用类型不匹配 → ClassCastException；未知字段 → RuntimeException；错误实例 → ClassCastException。
  - 并发：8 线程对共享 `volatile Integer` 字段 updateAndGet 自增，最终值 == N*M。

### 本轮契约深化追加

- 消息精确断言："Must be volatile type" / static "Must not be a static field"；private 跨类（Thread.name）→ RuntimeException；null 参数 → RuntimeException（`testValidationMessages`）。
- **identity 语义**：equals 相等不同对象 CAS 失败、null 期望分支（`testIdentitySemantics`）。
- 多对象互不干扰（`testMultiObjectIndependence`）。

断言规模：PASS (40 assertions)。
