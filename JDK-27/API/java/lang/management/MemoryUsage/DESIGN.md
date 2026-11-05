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

## 5. 契约深化（本批）
- 构造矩阵负例精确异常（实证 jdk8u372/jdk-27.0.1 一致）：init/used 任一为负 → IAE `init must be non-negative or -1` / `used must be non-negative`；committed 为负 → IAE（-1 除外）；used > committed → IAE `used should be <= committed`。
- `from(null)` 返回 null 而非抛 NPE（任务书原猜测被实证推翻）。
- `from(CompositeData)` 四字段全量往返：init/used/committed/max 与构造值逐一相等；非法字段名 → IAE。
- toString 精确格式 `init = X used = X committed = X max = X`（无千分位、无单位）。
- equals/hashCode 值语义：同值实例相等且 hashCode 相等，任一字段不同则不等。
