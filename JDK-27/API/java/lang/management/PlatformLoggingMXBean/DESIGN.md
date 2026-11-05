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

## 5. 契约深化（本批）
- ObjectName 精确 `java.lang:type=PlatformLogging`；单例身份（重复 getPlatformMXBean 同一实例）。
- 跨 API 闭环：setLoggerLevel(name, "INFO") 后 java.util.logging.Logger.getLogger(name).getLevel().getName() == "INFO"，反向 getLoggerLevel(name) 亦一致。
- setLoggerLevel(name, null) → level 置 null 继承 parent；getParentLoggerName 闭环。
- 负例：未注册 logger 的 getLoggerLevel → null；setLoggerLevel 不存在 logger 不抛（懒创建语义，实证）。
