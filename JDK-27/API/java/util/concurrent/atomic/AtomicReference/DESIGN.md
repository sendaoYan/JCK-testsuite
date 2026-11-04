# java.util.concurrent.atomic.AtomicReference 测试设计

## 1. API 功能
- 构造器：`AtomicReference()`（null）、`AtomicReference(V)`。
- 读/写：`get()`、`set(V)`、`lazySet(V)`。
- 原子更新：`getAndSet`、`compareAndSet`、`weakCompareAndSet*`、`compareAndExchange*`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 内存序访问器：`getPlain/setPlain`、`getOpaque/setOpaque`、`getAcquire/setRelease`。
- 转换：`toString()`（`String.valueOf(get())`）。实现 `Serializable`。

## 2. Java 用户如何使用
- 无锁引用持有：`AtomicReference<Node> head = new AtomicReference<>();`。
- CAS 替换：`head.compareAndSet(oldNode, newNode)`。
- 函数式更新：`r.updateAndGet(list -> list.append(x))`、`r.accumulateAndGet(x, op)`。
- 组合链路：`set(a) → getAndSet(b) → compareAndSet(b, c) → getAndUpdate`。

## 3. 开源/源码实际用法调研
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicReferenceTest.java`：
  `updateAndGet(x -> x + 2)`、`getAndUpdate(x -> x + 3)` 断言旧值/新值。
- JDK 源码 `java/lang/ClassLoader.java` 注释：用 `AtomicReferenceFieldUpdater`（引用 updater 同族）。
- 开源（Guava `AtomicDouble` 内部、Netty 链表头）广泛用于无锁数据结构。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicReferenceTest.testBlackBox()`）：
  - 构造默认 null/指定值；set/get/lazySet/getAndSet。
  - compareAndSet/weakCompareAndSet 成功失败；weak 用有界重试。
  - updateAndGet/getAndUpdate/accumulateAndGet/getAndAccumulate。
  - toString 对 null 与值的行为。
  - 内存序访问器 + compareAndExchange 见证值。
  - 并发：8 线程 updateAndGet(Integer 自增) 10 万次，最终值 == N*M。

### 本轮契约深化追加

- **identity 语义**：equals() 相等不同对象 CAS/compareAndExchange 失败、getAndSet 返回精确旧引用、null 期望分支（`testIdentitySemantics`）。
- 函数收到旧引用（getAndUpdate/accumulateAndGet 副作用列表）（`testFunctionReceivesOldReference`）。
- 序列化 round-trip：反序列化重建 String → 驻留字面量 identity CAS 失败（须 copy.get() 作期望）（`testSerialization`）。
- weakCompareAndSet @Deprecated（`testDeprecationContract`）。

断言规模：PASS (46 assertions)。
