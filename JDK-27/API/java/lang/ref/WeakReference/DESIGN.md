# java.lang.ref.WeakReference — 设计方案

## 1. API 功能
弱引用：不阻止 GC 回收（只要无强路径）：
- 构造：`WeakReference(T)`、`WeakReference(T, ReferenceQueue<? super T>)`（queue 可为 null，表示永不入队）
- 查询：继承 `get()/refersTo(T)/isEnqueued()`；操作：`clear()/enqueue()`

## 2. Java 用户如何使用
规范映射（WeakHashMap 内部）与规范化缓存：
```java
ReferenceQueue<K> queue = new ReferenceQueue<>();
WeakReference<K> keyRef = new WeakReference<>(key, queue);
if (keyRef.get() == key) { /* key 仍被外部持有 */ }
keyRef.enqueue();  // 显式入队；q.poll() 取回
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/util/WeakHashMap.java`：Entry extends WeakReference，配合 ReferenceQueue 在 resize/get 时 expunge 失效条目。
- JDK `test/jdk/java/lang/ref/Basic.java`：WeakReference+queue 的 enqueue/poll 基础行为。
- Guava `MapMaker().weakKeys()` 底层（`com.google.common.collect/LocalCache`）同样以 WeakReference 包装 key。
- Netty `Recycler`（`common/src/main/java/io/netty/util/concurrent/...`）以 WeakReference 持有线程局部对象。

## 4. 测试用例设计 + 自动化实现
白盒：具体类、`extends Reference`、两个 public 构造器、不覆写 `get()`。
黑盒（强可达 + 显式 enqueue，不依赖 GC）：
- get/refersTo 返回同一对象；
- 模拟 WeakHashMap 模式：带 queue 构造→手动 enqueue→poll 同一引用；`enqueue()` 实现先清 referent（get 变 null、refersTo false）；
- clear 后 get null 且不入队；
- null 引用对象 + null 队列组合合法，enqueue 恒 false；
- 以 `Reference<T>` 视角使用与类型回转。
对应 `WeakReferenceTest.java` 的 `testBlackBox()`。
