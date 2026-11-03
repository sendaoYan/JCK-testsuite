# java.util.concurrent.atomic.AtomicLongFieldUpdater 测试设计

## 1. API 功能
- 工厂：`static <U> newUpdater(Class<U>, String)`。
- 抽象方法：`compareAndSet(T,long,long)`、`weakCompareAndSet(T,long,long)`、`set(T,long)`、
  `lazySet(T,long)`、`get(T)`。
- 默认方法：`getAndSet`、`getAndIncrement/Decrement/Add`、`incrementAndGet/DecrementAndGet/AddAndGet`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 保护构造器；抽象类；非 `Serializable`。

## 2. Java 用户如何使用
- 对既有对象中的 `volatile long` 字段做原子更新（如累计值、版本号）：
  `AtomicLongFieldUpdater.newUpdater(Stat.class, "count")`，再 `u.addAndGet(stat, delta)`。
- 约束：字段必须 `volatile long`；实例类型不符抛 `ClassCastException`。
- 组合链路：`newUpdater → set → getAndAdd → compareAndSet`。

## 3. 开源/源码实际用法调研
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java`：验证 public/private long
  字段与跨包字段的 updater 可创建性。
- JDK 源码 `java/sql/SQLException.java` 使用 `AtomicReferenceFieldUpdater`（引用 updater 同族，
  展示 field updater 形态）。
- JDK 源码 `java/nio/Bits.java` 使用 `AtomicLong`（标量同族）。
- 开源（Netty）常用 `AtomicLongFieldUpdater` 做低成本并发计数器。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicLongFieldUpdaterTest.testBlackBox()`）：
  - 完整字段更新操作链。
  - 两个 updater 指向不同字段相互独立。
  - 异常：非 volatile → IllegalArgumentException；int 字段 → IllegalArgumentException（必须 long）；
    未知字段 → RuntimeException；错误实例 → ClassCastException。
  - 并发：8 线程对共享 `volatile long` 字段 incrementAndGet，最终值 == N*M。
