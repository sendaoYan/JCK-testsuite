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

## 5. 契约深化（本批）
- values() 内容恒为 {HEAP, NON_HEAP} 且顺序稳定；返回数组是防御性副本（改动不影响后续调用）。
- toString() == name()（"HEAP"/"NON_HEAP"，非 "Heap memory"）；valueOf 大小写敏感，`valueOf("heap")` → IllegalArgumentException。
- Enum 用 identity hashCode：`HEAP.hashCode() != "HEAP".hashCode()`（本批踩坑实证后修正断言为稳定性检查）；equals 为枚举身份。
- 两侧（JDK-8/27）该类型无版本差异。
