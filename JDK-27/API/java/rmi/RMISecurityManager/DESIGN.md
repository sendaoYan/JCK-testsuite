# java.rmi.RMISecurityManager 行为测试设计

## 1. API 功能
`RMISecurityManager` 继承 `java.lang.SecurityManager`，`@Deprecated(forRemoval=true)`。JDK 27 中其构造器为空，仅作为遗留兼容类存在。

## 2. Java 用户如何使用
- 遗留代码 `System.setSecurityManager(new RMISecurityManager())`（现代 JDK 中 setSecurityManager 抛 UnsupportedOperationException）。
- 本测试仅验证 `new RMISecurityManager()` 可构造、类型正确。

## 3. 开源代码实际用法调研
- 类定义 `src/java.rmi/share/classes/java/rmi/RMISecurityManager.java`：构造器体为空，注释说明「此特性已不存在」。
- 现代 RMI 测试不再设置安全管理器（SecurityManager 已废弃/移除）。

## 4. 测试用例设计 + 自动化实现
黑盒部分（RMISecurityManagerTest.testBlackBox）：`new RMISecurityManager()` 非空、`instanceof SecurityManager`、两实例不同且不 value-equal。
白盒部分（testWhiteBox）：继承 SecurityManager、非抽象/非 final、无接口、@Deprecated、单个无参构造器。
