# PlatformLoggingMXBean — 设计方案

## 1. API 功能
`java.lang.management.PlatformLoggingMXBean`（接口，继承 `PlatformManagedObject`）暴露 `java.util.logging` 日志配置：`getLoggerNames()`/`getLoggerLevel(String)`/`setLoggerLevel(String,String)`/`getParentLoggerName(String)`。

## 2. Java 用户如何使用
```java
PlatformLoggingMXBean log = ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
log.setLoggerLevel("com.example", "FINE");
String level = log.getLoggerLevel("com.example");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/PlatformLoggingMXBean` 验证 set/get level 往返与 parent 解析。
- 运维工具用其远程调整运行时日志级别（JMX）。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 四个方法返回类型。
黑盒：创建父子 logger 后 getLoggerNames 包含；setLoggerLevel/getLoggerLevel 往返；getParentLoggerName 解析；reset null；objectName 非空。
对应 `PlatformLoggingMXBeanTest.java` 白盒 + 黑盒部分。
