# MemoryManagerMXBean — 设计方案

## 1. API 功能
`java.lang.management.MemoryManagerMXBean`（接口，继承 `PlatformManagedObject`）描述内存管理器：`getName()`、`isValid()`、`getMemoryPoolNames()`。

## 2. Java 用户如何使用
```java
for (MemoryManagerMXBean m : ManagementFactory.getMemoryManagerMXBeans())
    System.out.println(m.getName() + " -> " + Arrays.toString(m.getMemoryPoolNames()));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/MemoryManagerMXBean` 断言名称非空、valid、池名非空。
- jconsole 用其展示 GC/CodeCache/Metaspace 管理器及所管内存池。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 三个方法返回类型。
黑盒：`getMemoryManagerMXBeans()` 非空；每管理器 name 非空/valid/poolNames 非空/objectName 非空；名称排序互异。
对应 `MemoryManagerMXBeanTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- GC 是 manager 子集：getGarbageCollectorMXBeans 的名称集 ⊆ getMemoryManagerMXBeans 名称集。
- 逐 manager 契约：getName() 非空、isValid() == true、getMemoryPoolNames() 为数组（可为空但非 null）。
- ObjectName canonical `java.lang:name=X,type=MemoryManager`。
