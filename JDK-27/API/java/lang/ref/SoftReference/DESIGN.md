# java.lang.ref.SoftReference — 设计方案

## 1. API 功能
软引用：内存在不足前保证引用可达（行为由 GC 策略决定，API 层与 WeakReference 相同）：
- 构造：`SoftReference(T)`、`SoftReference(T, ReferenceQueue<? super T>)`
- 查询：`get()`（覆写，返回 T）、继承 `refersTo/isEnqueued/clear/enqueue`

## 2. Java 用户如何使用
内存敏感缓存：值用 SoftReference 包住，OOM 前由 GC 释放：
```java
Map<String, SoftReference<byte[]>> cache = new HashMap<>();
cache.put("blob", new SoftReference<>(bytes));
byte[] b = cache.get("blob").get();
if (b == null) { b = reload(); cache.put("blob", new SoftReference<>(b)); }
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/ref/SoftReference.java`：仅覆写 `get()`/时钟字段，清除时机由 VM 的 soft-ref policy 决定。
- JDK `test/jdk/java/lang/ref/SoftReference/*.java`（如 `SoftReference.java` 基础用例）验证构造/get/queue 语义。
- Apache Commons JCS、Ehcache 的 disk-backed 层用 SoftReference 做内存层条目（文档明确“不保证存活”）。

## 4. 测试用例设计 + 自动化实现
白盒：具体类、`extends Reference`、两个 public 构造器、覆写 `get()`。
黑盒（强可达路径，不断言 GC 清除时机——那是非确定性的）：
- 构造+get 同一对象、`refersTo`、未入队；
- 两个独立 SoftReference 指向同一对象、equals 身份语义；
- 带队列构造 + 手动 enqueue→poll→clear→get null；
- null 引用对象合法；以 `Reference<T>` 静态视角使用；与 WeakReference 类型互斥。
对应 `SoftReferenceTest.java` 的 `testBlackBox()`。
