# GarbageCollectorMXBean — 设计方案

## 1. API 功能
`java.lang.management.GarbageCollectorMXBean`（接口，继承 `MemoryManagerMXBean`）暴露 GC 统计：`getCollectionCount()`/`getCollectionTime()`；继承 `getName()`/`isValid()`/`getMemoryPoolNames()`。

## 2. Java 用户如何使用
```java
for (GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans())
    System.out.println(g.getName() + " " + g.getCollectionCount());
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/GarbageCollectorMXBean` 断言名称非空、计数/时间非负。
- jconsole 的 GC 标签页用其展示各收集器次数/耗时。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 MemoryManagerMXBean + 两个方法返回类型。
黑盒：`getGarbageCollectorMXBeans()` 非空；每 GC 名称非空/计数/时间非负/有效/管理至少一池/objectName 非空；名称排序后互异。
对应 `GarbageCollectorMXBeanTest.java` 白盒 + 黑盒部分。
