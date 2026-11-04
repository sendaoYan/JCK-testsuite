# SecurityManager — 设计方案

## 1. API 功能
`java.lang.SecurityManager`（JDK 17 起 deprecated for removal，JEP 486 禁用）是旧式安全策略检查入口：
- 查询：`getSecurityContext/getThreadGroup`
- 检查：`checkPermission`、`checkRead/Write/Connect/Listen/Exit/Exec/...` 大量 `check*` 方法

## 2. Java 用户如何使用
- 现代 JDK 中 `System.setSecurityManager` 抛 `UnsupportedOperationException`，`getSecurityManager` 恒为 null。
```java
SecurityManager sm = new SecurityManager();
sm.checkPermission(new RuntimePermission("getClassLoader"));
```

## 3. 开源代码实际用法调研
- 旧式 applet/插件沙箱使用；JDK 17+ 已废弃，`System.setSecurityManager` 被永久禁用（JEP 486）。
- JDK `test/jdk/java/lang/SecurityManager` 系列验证检查与上下文。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.SecurityManager")` 验证非接口、父类 `Object`、无参构造器。
黑盒：构造后 `getThreadGroup`/`getSecurityContext`（返回 AccessControlContext）；JEP 486 后所有默认 `check*`（checkPermission/checkExit/checkCreateClassLoader/checkRead）一律抛 `SecurityException`；`System.getSecurityManager()` 为 null、`setSecurityManager` 抛 `UnsupportedOperationException`。
对应 `<Simple>Test.java` 黑盒部分。
