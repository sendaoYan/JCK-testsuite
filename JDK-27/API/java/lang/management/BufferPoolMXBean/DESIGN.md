# BufferPoolMXBean — 设计方案

## 1. API 功能
`java.lang.management.BufferPoolMXBean`（接口，继承 `PlatformManagedObject`）描述 NIO 缓冲池：`getName()`/`getCount()`/`getTotalCapacity()`/`getMemoryUsed()`。通过 `ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)` 获取。

## 2. Java 用户如何使用
```java
for (BufferPoolMXBean b : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class))
    System.out.println(b.getName() + " used=" + b.getMemoryUsed());
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/BufferPoolMXBean` 断言 direct/mapped 池计数与容量非负。
- 监控工具用 BufferPoolMXBean 观测 direct buffer 占用。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 四个方法返回类型。
黑盒：`getPlatformMXBeans(BufferPoolMXBean.class)` 非空；每池 name 非空、count/capacity/used 非负、objectName 非空；存在 direct 池。
对应 `BufferPoolMXBeanTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- ObjectName canonical 精确 `java.nio:name=X,type=BufferPool`（键按字典序排序——本批实证踩坑：toString 为插入序 `type=...,name=...`，getCanonicalName 为 `name=...,type=...`）。
- 列表稳定：getPlatformMXBeans 恒含 direct/mapped 两个池，重复调用内容一致。
- 分配记账（弱断言）：ByteBuffer.allocateDirect(n) 后 direct 池 getCount() 增长、getMemoryUsed() 增量 >= n；getCapacity/getTotalCapacity >= 0。
