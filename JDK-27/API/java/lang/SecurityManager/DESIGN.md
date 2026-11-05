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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）

- **JEP 486 stub 化实证**：实例化 SecurityManager 后**所有** `check*`（checkPermission、checkExit、checkRead、checkConnect、checkPackageAccess、checkPropertyAccess、checkThreadAccess…）无条件抛 `SecurityException`——即使参数 null 也抛且**消息为 null**（无参数校验先行）。
- **setSecurityManager 桩**（仅反射检查，断言其元数据、不依赖调用）：`System.setSecurityManager` 仍声明，static void，`@Deprecated(since="17", forRemoval=true)`；调用本体抛 `UnsupportedOperationException`（既有用例已覆盖调用点）。
- **残存可用方法**：`getSecurityContext()` 仍返回 `AccessControlContext`；`getClassContext()` 仍返回真实调用链（首帧即 SM 子类自身）。
- **类形态**：非 final、单公开构造器、`getSecurityManager()==null`。

### 2. 使用方式

```java
SecurityManager sm = new SecurityManager();   // 构造合法
sm.checkPermission(null);                     // 27: SecurityException(null message)
sm.getClassContext();                         // 仍真实可用
```

### 3. 参考来源

- 实测 Temurin jdk-27.0.1.0.2+1（探针 /tmp/lcF）：全部 check* 的抛出类型与消息空值实测后锁。
- JEP 486（Permanent Disabling of the Security Manager）。
- JDK-27 javadoc SecurityManager（deprecation 标注）。

### 4. 测试用例设计（映射到 SecurityManagerTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| setSecurityManager 存在/static/@Deprecated(17,forRemoval) | testStubSurface |
| checkPermission(null) → SE 且消息为 null | testStubNullArguments |
| 任意上下文 checkPermission 均 SE；getSecurityContext 仍 ACC | testStubContextChecks |
| 其余 check* 全部无条件 SE | testStubRemainingChecks |
| getClassContext 首帧 | testClassContextStub |

- **易错**：27 侧 SE 消息是 **null**，不是参数校验 NPE——stub 不做任何参数检查。
- **易错**：`getSecurityContext`/`getClassContext` 未 stub 化，仍可真实断言。
- **易错**：测试**绝不调用 `System.setSecurityManager` 影响 8 侧行为**；27 侧仅反射检查元数据。
