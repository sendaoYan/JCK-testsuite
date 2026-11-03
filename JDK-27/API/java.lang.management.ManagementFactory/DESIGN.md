# ManagementFactory — 设计方案

## 1. API 功能
`java.lang.management.ManagementFactory`（类）是平台 MXBean 的工厂入口：`getRuntimeMXBean`/`getMemoryMXBean`/`getThreadMXBean`/`getOperatingSystemMXBean`/`getClassLoadingMXBean`/`getCompilationMXBean`；列表 `getMemoryPoolMXBeans`/`getMemoryManagerMXBeans`/`getGarbageCollectorMXBeans`；`getPlatformMBeanServer()`；`getPlatformMXBean(s)`；`getPlatformManagementInterfaces()`；`newPlatformMXBeanProxy`；常量 `*_MXBEAN_NAME`。

## 2. Java 用户如何使用
```java
RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
MBeanServer server = ManagementFactory.getPlatformMBeanServer();
List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management` 全部用例都经 `ManagementFactory` 取 MXBean。
- Spring Boot Actuator 的 JMX 端点、Micrometer 用 `getPlatformMBeanServer` 注册/查询 MBean。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 各 getter 返回类型 + getPlatformMBeanServer/getPlatformManagementInterfaces 返回类型。
黑盒：各 getXxxMXBean 非空；列表非空；MBeanServer 单例；平台接口集合含 Runtime/Memory/Thread/OS；getPlatformMXBean(s) 非空；newPlatformMXBeanProxy 往返 vmName；常量非空。
对应 `ManagementFactoryTest.java` 白盒 + 黑盒部分。
