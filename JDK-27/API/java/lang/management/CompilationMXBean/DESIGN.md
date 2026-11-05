# CompilationMXBean — 设计方案

## 1. API 功能
`java.lang.management.CompilationMXBean`（接口，继承 `PlatformManagedObject`）暴露 JIT 编译信息：`getName()`、`isCompilationTimeMonitoringSupported()`、`getTotalCompilationTime()`。

## 2. Java 用户如何使用
```java
CompilationMXBean b = ManagementFactory.getCompilationMXBean();
String compiler = b.getName();
long time = b.getTotalCompilationTime();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/CompilationMXBean` 断言 name 非空、time 非负。
- 监控工具用其展示 JIT 编译器与累计编译耗时。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 三个方法返回类型。
黑盒：name 非空、objectName 非空；按是否支持监控断言 totalCompilationTime 非负或为 0。
对应 `CompilationMXBeanTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- ObjectName 精确 `java.lang:type=Compilation,name=<getName()>`。
- getName() 非空且两次调用稳定；HotSpot 为 "HotSpot 64-Bit Tiered Compilers" 类名称（不断言具体值，只断稳定+非空）。
- isCompilationTimeMonitoringSupported() 为真时 getTotalCompilationTime() >= 0 且两次采样单调不减（弱断言防 flaky）。
