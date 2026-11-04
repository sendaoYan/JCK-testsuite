# MemoryUsage — 设计方案

## 1. API 功能
`java.lang.management.MemoryUsage`（类）封装内存用量快照：`getInit()`/`getUsed()`/`getCommitted()`/`getMax()`（-1 表示未定义）；`toString()`；静态 `from(CompositeData)`；构造 `(long,long,long,long)`。

## 2. Java 用户如何使用
```java
MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
long used = heap.getUsed();
long max  = heap.getMax(); // 可能 -1
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/MemoryMXBean` 断言 used/committed/max 关系。
- jconsole/JMC 用 MemoryUsage 展示堆/非堆/池用量。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 Object + 4-long 构造器 + 四个 getter + from。
黑盒：构造后四个 getter 精确往返；-1 表示未定义 init/max；toString 含数值。
对应 `MemoryUsageTest.java` 白盒 + 黑盒部分。
