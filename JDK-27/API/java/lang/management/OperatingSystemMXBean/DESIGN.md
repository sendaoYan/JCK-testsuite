# OperatingSystemMXBean — 设计方案

## 1. API 功能
`java.lang.management.OperatingSystemMXBean`（接口，继承 `PlatformManagedObject`）暴露操作系统信息：`getName()`/`getArch()`/`getVersion()`/`getAvailableProcessors()`/`getSystemLoadAverage()`。

## 2. Java 用户如何使用
```java
OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
int cpus = os.getAvailableProcessors();
double load = os.getSystemLoadAverage();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/OperatingSystemMXBean` 断言 name/arch/version 非空、处理器数 >=1。
- Micrometer 用其采集 CPU 数与系统负载指标。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 五个方法返回类型。
黑盒：name/arch/version 非空、availableProcessors>=1、loadAverage>=-1.0（Windows 返回 -1）、objectName 非空。
对应 `OperatingSystemMXBeanTest.java` 白盒 + 黑盒部分。
