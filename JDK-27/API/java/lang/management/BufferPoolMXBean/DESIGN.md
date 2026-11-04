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
