# java.util.concurrent.atomic.AtomicIntegerFieldUpdater 测试设计

## 1. API 功能
- 工厂：`static <U> newUpdater(Class<U>, String)`。
- 抽象方法：`compareAndSet(T,int,int)`、`weakCompareAndSet(T,int,int)`、`set(T,int)`、
  `lazySet(T,int)`、`get(T)`。
- 默认方法：`getAndSet`、`getAndIncrement/Decrement/Add`、`incrementAndGet/DecrementAndGet/AddAndGet`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 保护构造器；抽象类；非 `Serializable`。

## 2. Java 用户如何使用
- 对既有对象中的 `volatile int` 字段做原子更新（如节点引用计数）：
  `AtomicIntegerFieldUpdater.newUpdater(Node.class, "refCount")`，再 `u.incrementAndGet(node)`。
- 约束：字段必须 `volatile int`，否则 `newUpdater` 抛异常；实例类型不符抛 `ClassCastException`。
- 组合链路：`newUpdater → get → compareAndSet → getAndIncrement`。

## 3. 开源/源码实际用法调研
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java`：矩阵式验证不同
  可访问性（public/private、本类/跨包）字段能否创建 updater。
- JDK 源码 `java/lang/ClassLoader.java` 注释引用 `AtomicReferenceFieldUpdater`（引用 updater 同族）。
- JDK 源码 `jdk.internal` 中大量用 `AtomicIntegerFieldUpdater` 避免每个字段一个原子对象的内存开销。
- 开源（Netty/Disruptor）用 field updater 减少对象头开销。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicIntegerFieldUpdaterTest.testBlackBox()`）：
  - 完整字段更新操作链（set/get/compareAndSet/weakCompareAndSet/getAndSet/increment/decrement/add/
    update/accumulate/lazySet）。
  - 两个 updater 指向同一对象不同字段相互独立。
  - 异常：非 volatile → IllegalArgumentException；long 字段 → IllegalArgumentException（必须 int）；
    未知字段 → RuntimeException；错误实例 → ClassCastException。
  - 并发：8 线程对共享 `volatile int` 字段 incrementAndGet，最终值 == N*M。

### 本轮契约深化追加

- **消息精确断言**："Must be volatile type" / "Must be integer type" / static 字段 "Must not be a static field"（JDK-27 实测；JDK-8 static 消息为 null）（`testValidationMessages`）。
- private 跨类（java.base AtomicInteger.value）→ RuntimeException；null 字段名/null 类 → RuntimeException。注：JDK-11+ 嵌套类是 nestmate 可访问，故用跨类负例（`testValidationMessages`）。
- 多对象互不干扰（`testMultiObjectIndependence`）。

断言规模：PASS (42 assertions)。
