# RuntimeMXBean — 设计方案

## 1. API 功能
`java.lang.management.RuntimeMXBean`（接口，继承 `PlatformManagedObject`）暴露 JVM 运行时信息：`getVmName`/`getVmVendor`/`getVmVersion`、`getSpecName`/`getSpecVendor`/`getSpecVersion`、`getName`/`getPid`/`getUptime`/`getStartTime`、`getInputArguments`/`getSystemProperties`/`getClassPath`/`getLibraryPath`/`getBootClassPath`。

## 2. Java 用户如何使用
```java
RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
long pid = r.getPid();
Map<String,String> props = r.getSystemProperties();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/RuntimeMXBean` 断言 vmName/specName/pid/uptime 等。
- Spring Boot 的 `ApplicationPid`、Micrometer 用 `getPid()`/`getStartTime()` 采集 JVM 信息。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 关键方法返回类型。
黑盒：vmName/specVersion/specName/vmVersion/name 非空；pid>0、uptime>=0、startTime>0；inputArguments/classPath/libraryPath 非空；systemProperties 含 java.version；objectName 非空。
对应 `RuntimeMXBeanTest.java` 白盒 + 黑盒部分。
