# MemoryType — 设计方案

## 1. API 功能
`java.lang.management.MemoryType`（枚举）区分堆/非堆内存：`HEAP`、`NON_HEAP`；`toString()` 返回可读描述 "Heap memory"/"Non-heap memory"；继承 `Enum` 的 `values()`/`valueOf()`。

## 2. Java 用户如何使用
```java
for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans())
    if (p.getType() == MemoryType.HEAP) { ... }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/MemoryPoolMXBean` 按 `getType()` 分类堆/非堆池断言。
- jconsole/JMC 用 MemoryType 区分堆与非堆内存池展示。

## 4. 测试用例设计 + 自动化实现
白盒：枚举 + 继承 Enum + 两个常量 + values/valueOf 返回类型。
黑盒：values 长度 2；valueOf HEAP/NON_HEAP；name/toString/ordinal；valueOf bogus 抛 IAE。
对应 `MemoryTypeTest.java` 白盒 + 黑盒部分。
