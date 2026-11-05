# MemoryPoolMXBean — 设计方案

## 1. API 功能
`java.lang.management.MemoryPoolMXBean`（接口，继承 `PlatformManagedObject`）描述内存池：`getName()`/`getType()`/`getUsage()`/`getPeakUsage()`/`resetPeakUsage()`/`getMemoryManagerNames()`/`isValid()` 及 usage/collection 阈值相关方法。

## 2. Java 用户如何使用
```java
for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans())
    System.out.println(p.getName() + " " + p.getType() + " used=" + p.getUsage().getUsed());
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/MemoryPoolMXBean` 断言类型、usage/peak、阈值支持性。
- jconsole/Micrometer 用其按池采集堆与非堆用量。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 关键方法返回类型。
黑盒：池列表非空；每池 name 非空/type HEAP 或 NON_HEAP/usage/peak used 非负/manager 名非空/valid/objectName 非空；阈值方法按支持性断言非负；堆与非堆池均存在。
对应 `MemoryPoolMXBeanTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- 逐池契约：getType() ∈ {HEAP, NON_HEAP}；getUsage() 非空且 used <= committed（弱断言）；isValid() == true；getName() 非空且稳定。
- 阈值生命周期：isUsageThresholdSupported() 为真的池 setUsageThreshold(v) 回读一致；setUsageThreshold(负数) → IAE（消息含 threshold 字样）；不支持的池 setUsageThreshold → UnsupportedOperationException。
- collectionUsageThreshold 平行路径：supported 判定与 set/getThreshold 往返。
- resetPeakUsage() 后 getPeakUsage().used >= getUsage().used 且不小于复位时刻 usage（峰值语义弱断言）。
- getMemoryManagerNames() 非空（每个池至少一个管理器）。
