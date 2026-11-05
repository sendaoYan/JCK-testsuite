# ClassLoadingMXBean — 设计方案

## 1. API 功能
`java.lang.management.ClassLoadingMXBean`（接口，继承 `PlatformManagedObject`）暴露类加载统计：`getTotalLoadedClassCount()`/`getLoadedClassCount()`/`getUnloadedClassCount()`/`isVerbose()`/`setVerbose(boolean)`。

## 2. Java 用户如何使用
```java
ClassLoadingMXBean b = ManagementFactory.getClassLoadingMXBean();
long loaded = b.getLoadedClassCount();
b.setVerbose(false);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/management/ClassLoadingMXBean` 断言计数非负且 loaded<=total。
- 监控工具用其观测类加载压力。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 PlatformManagedObject + 各方法返回类型。
黑盒：计数非负/>=1、objectName 非空；setVerbose(false) 生效并恢复原值。
对应 `ClassLoadingMXBeanTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- ObjectName 精确 `java.lang:type=ClassLoading`（canonical）。
- 计数单调与序关系：显式 loadClass N 个后 getLoadedClassCount 增长 >= N；getTotalLoadedClassCount() >= getLoadedClassCount()（unload >= 0）；getUnloadedClassCount() >= 0。
- setVerbose(true/false) 往返且 getVerbose() 回读一致。
- 单例身份：两次 getClassLoadingMXBean() 同一实例。
