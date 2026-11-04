# MemoryNotificationInfo — 设计方案

## 1. API 功能
`java.lang.management.MemoryNotificationInfo`（类）封装内存阈值通知：`getPoolName()`/`getUsage()`/`getCount()`；常量 `MEMORY_THRESHOLD_EXCEEDED`/`MEMORY_COLLECTION_THRESHOLD_EXCEEDED`；构造 `(String, MemoryUsage, long)`；静态 `from(CompositeData)`。

## 2. Java 用户如何使用
```java
MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(type)) { ... }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/MemoryMXBean` 注册阈值通知并解析 `MemoryNotificationInfo`。
- 监控工具解析 `java.lang:type=Memory` 的阈值通知 CompositeData。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 Object + 构造器 + getter + 两个常量。
黑盒：构造后 getPoolName/getUsage/getCount 往返；两个通知类型常量非空且互异。
对应 `MemoryNotificationInfoTest.java` 白盒 + 黑盒部分。
