# java.util.concurrent.atomic.AtomicReferenceArray 测试设计

## 1. API 功能
- 构造器：`AtomicReferenceArray(int)`、`AtomicReferenceArray(E[])`（复制）。
- 查询：`length()`。
- 按索引读/写：`get(int)`、`set(int,E)`、`lazySet(int,E)`。
- 按索引原子更新：`getAndSet`、`compareAndSet`、`weakCompareAndSet*`、`compareAndExchange*`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 内存序访问器：`getPlain/setPlain`、`getOpaque/setOpaque`、`getAcquire/setRelease`。
- 转换：`toString()`。实现 `Serializable`。

## 2. Java 用户如何使用
- 引用槽位无锁更新：`AtomicReferenceArray<String> cache = new AtomicReferenceArray<>(n); cache.set(i, v);`
- CAS 替换槽位：`cache.compareAndSet(i, old, updated)`。
- 函数式更新：`cache.updateAndGet(i, s -> s + suffix)`。
- 组合链路：`set(i, a) → compareAndSet(i, a, b) → getAndUpdate(i, ...)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/time/ZoneOffset.java`：`AtomicReferenceArray<ZoneOffset> QUARTER_CACHE = new AtomicReferenceArray<>(256);`
- JDK 源码 `sun/util/locale/provider/HostLocaleProviderAdapterImpl.java`：`AtomicReferenceArray<String>`
  缓存日期/数字格式模式（构造后按索引 get/set）。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java` 同族参照。
- 开源（Netty/Caffeine）常用 `AtomicReferenceArray` 做无锁槽位缓存与状态数组。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicReferenceArrayTest.testBlackBox()`）：
  - length、初始 null、set/get、getAndSet、lazySet。
  - compareAndSet/weakCompareAndSet 成功失败；weak 用有界重试。
  - updateAndGet/getAndUpdate/accumulateAndGet/getAndAccumulate（字符串拼接）。
  - 内存序访问器 + compareAndExchange 见证值。
  - E[] 构造复制语义、toString、越界/负长度异常。
  - 并发：8 线程按索引 updateAndGet(Integer) 自增，每槽位最终值 == N*M/4。

### 本轮契约深化追加

- **identity 语义**：索引 CAS/compareAndExchange 期望 equals 相等不同对象失败；null 元素契约（初始 null、CAS(null→v)、set(null)、exchange null 见证）（`testIdentitySemantics`）。
- 越界全矩阵（ArrayIndexOutOfBoundsException：compareAndSet/getAndSet/lazySet/getAndUpdate/getAcquire/setRelease/compareAndExchange/getPlain）（`testIndexBoundsFull`）。
- 序列化 round-trip（用实际存储引用做 identity CAS）（`testSerialization`）。

断言规模：PASS (66 assertions)。
