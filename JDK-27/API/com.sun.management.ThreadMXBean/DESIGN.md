# com.sun.management.ThreadMXBean 测试设计

## 1. API 功能

- 接口：`com.sun.management.ThreadMXBean extends java.lang.management.ThreadMXBean`。
- 数组版 CPU 时间：`getThreadCpuTime(long[] ids)`、`getThreadUserTime(long[] ids)`（每项为对应线程纳秒 CPU 时间；线程死亡/未启用返回 -1；不支持抛 UnsupportedOperationException）。
- 分配内存：`getThreadAllocatedBytes(long id)`、`getThreadAllocatedBytes(long[] ids)`、default `getCurrentThreadAllocatedBytes()`、default `getTotalThreadAllocatedBytes()`。
- 开关：`isThreadAllocatedMemorySupported()`、`isThreadAllocatedMemoryEnabled()`（不支持时抛 UnsupportedOperationException）、`setThreadAllocatedMemoryEnabled(boolean)`。
- 继承单线程 CPU 方法：`getCurrentThreadCpuTime()`、`getCurrentThreadUserTime()`、`getThreadCpuTime(long)`、`getThreadUserTime(long)`、`isThreadCpuTimeSupported()`、`isThreadCpuTimeEnabled()`、`setThreadCpuTimeEnabled(boolean)`。

## 2. Java 用户如何使用

`ManagementFactory.getThreadMXBean()` cast 到 `com.sun.management.ThreadMXBean`，启用 CPU 时间/分配内存测量后按线程 id 查询。JDK 用 `Thread.currentThread().threadId()`（不是已废弃的 `getId()`）。组合链路：

```
com.sun.management.ThreadMXBean t = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
if (t.isThreadCpuTimeSupported() && !t.isThreadCpuTimeEnabled()) t.setThreadCpuTimeEnabled(true);
long cpu = t.getThreadCpuTime(Thread.currentThread().threadId());
if (t.isThreadAllocatedMemorySupported() && !t.isThreadAllocatedMemoryEnabled()) t.setThreadAllocatedMemoryEnabled(true);
long alloc = t.getThreadAllocatedBytes(Thread.currentThread().threadId());
```

## 3. 开源代码实际用法调研

来源 `~/git/jdk25u-dev`（源码 + test/jdk，WebSearch 不可用则以此为准）。

- `~/git/jdk25u-dev/test/jdk/com/sun/management/ThreadMXBean/ThreadAllocatedMemory.java`：先 `isThreadAllocatedMemorySupported()`，禁用后 `getCurrentThreadAllocatedBytes()` 返回 -1，再启用后断言 `>=0` 且分配后单调不减；并覆盖 `getThreadAllocatedBytes(long)`、`getThreadAllocatedBytes(long[])`、`getTotalThreadAllocatedBytes()`。
- `~/git/jdk25u-dev/test/jdk/com/sun/management/ThreadMXBean/ThreadCpuTimeArray.java`：禁用 CPU 时间后数组返回 -1，启用后 `>=0`；并断言 `getThreadCpuTime(null)` 抛 NPE、非法 id 抛 IllegalArgumentException。
- `~/git/jdk25u-dev/src/jdk.management/share/classes/com/sun/management/ThreadMXBean.java`：数组方法 javadoc 明确「线程存活且 CPU 测量启用才返回非负，否则 -1」「不支持抛 UnsupportedOperationException」；`isThreadAllocatedMemoryEnabled()` 不支持时抛 UnsupportedOperationException。

## 4. 测试用例设计 + 自动化实现

对应 `ThreadMXBeanTest.java` 黑盒部分：

1. `instanceof com.sun.management.ThreadMXBean`；`Thread.currentThread().threadId()` 取当前线程 id。
2. CPU 时间：若支持则确保启用（finally 恢复原状），断言 `getCurrentThreadCpuTime()/getCurrentThreadUserTime()/getThreadCpuTime(id)/getThreadUserTime(id) >= 0`；不支持则断言 `== -1`。数组版断言长度与 `getAllThreadIds()` 一致且每项 `>=-1`。
3. 分配内存：若支持则确保启用（finally 恢复），断言 `getThreadAllocatedBytes(id)/getCurrentThreadAllocatedBytes()/getTotalThreadAllocatedBytes() >= 0`，分配一个 `byte[4096]` 后断言分配量单调不减；不支持则断言 `== -1`。数组版断言长度一致且每项 `>=-1`。
