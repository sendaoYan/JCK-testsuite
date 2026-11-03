# PlatformManagedObject — 设计方案

## 1. API 功能
`java.lang.management.PlatformManagedObject`（接口）是平台 MXBean 的公共父接口，唯一方法 `getObjectName()` 返回该 MXBean 的 JMX ObjectName。

## 2. Java 用户如何使用
```java
ObjectName name = ManagementFactory.getRuntimeMXBean().getObjectName();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management` 用 `getObjectName()` 定位平台 MBean 的 ObjectName。
- `ManagementFactory.getPlatformManagementInterfaces()` 返回该接口的子接口集合。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + getObjectName 返回 ObjectName。
黑盒：匿名实现 getObjectName 往返；真实 RuntimeMXBean/MemoryMXBean 实现该接口且 objectName 非空。
对应 `PlatformManagedObjectTest.java` 白盒 + 黑盒部分。
