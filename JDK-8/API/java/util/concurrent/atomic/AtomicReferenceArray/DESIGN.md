# java.util.concurrent.atomic.AtomicReferenceArray 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`AtomicReferenceArray(int length)`（全 null）、`AtomicReferenceArray(E[] array)`（拷贝）。
- **索引读/写**：`length()`、`get(i)`、`set(i,v)`、`lazySet(i,v)`、`getAndSet(i,v)`。
- **索引原子更新**：`compareAndSet(i,e,u)`（**identity 语义**）、`weakCompareAndSet(i,e,u)`、
  `getAndUpdate(i,f)`/`updateAndGet(i,f)`、`getAndAccumulate(i,x,f)`/`accumulateAndGet(i,x,f)`。
- **toString** / **Serializable**。

## 2. Java 用户如何使用

- 无锁对象槽位数组（按索引换引用）；null 元素是合法状态。

## 3. 开源/源码实际用法调研

- OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicReferenceArrayTest.java`（含函数式更新）。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicReferenceArray`。

## 4. 测试用例设计（映射到 AtomicReferenceArrayTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **identity 语义：索引 CAS 期望 equals() 相等的不同对象失败** | testIdentitySemantics |
| P0 | null 元素契约（初始 null、CAS(null→v) 成功、set(null) 合法） | testIdentitySemantics |
| P0 | getAndSet 按索引返回精确旧引用 | testIdentitySemantics |
| P0 | E[] 构造器拷贝；0 长度；负长度 → NegativeArraySizeException | testConstructorsAndBounds |
| P0 | 索引越界（-1、length）→ IndexOutOfBoundsException（get/set/CAS/getAndSet/lazySet） | testConstructorsAndBounds |
| P0 | update/accumulate 按索引（函数收到该索引旧元素） | testUpdateFamily |
| P1 | 索引 0 与 1 互不干扰；toString 含元素 / "[]" | testUpdateFamily |
| P1 | 序列化 round-trip（**用实际存储引用做 identity CAS**） | testSerialization |
| P1 | JDK-9+ 索引化访问模式族缺席反射断言 | testAbsentJdk9Methods |

## 关键规范细节（易错点）

- 引用数组 CAS 全部 == 语义；null 是普通值参与匹配。
- 序列化重建 String 引用 → 驻留字面量 CAS 失败，须 `copy.get(i)` 作期望。

## 断言规模

PASS (54 assertions)。
