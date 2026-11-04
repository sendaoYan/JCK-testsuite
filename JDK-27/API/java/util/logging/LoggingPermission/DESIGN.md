# java.util.logging.LoggingPermission 测试方案

白盒结构断言在 `LoggingPermissionTest.testWhiteBox()`，黑盒行为断言在 `LoggingPermissionTest.testBlackBox()`。

## 1. API 功能

- **构造器**：`LoggingPermission(String name, String actions)`（JDK-27 仅此一个 public 构造器）。
- **继承**：`extends java.security.BasicPermission`，最终类（final）。
- **继承方法**：`getName()`、`getActions()`、`equals(Object)`、`hashCode()`、`implies(Permission)`、`toString()`。

## 2. Java 用户如何使用

- 构造权限：`new LoggingPermission("control", null)`（actions 未用，可为 null/空）。
- 权限校验（有 SecurityManager 时）：`perm.implies(otherPerm)`、`AccessController.checkPermission(perm)`。
- 判等/集合：`new LoggingPermission("control", null).equals(new LoggingPermission("control", null))`。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/LoggingPermission.java`：定义处（extends BasicPermission，构造器转交 super）。
- `src/java.logging/share/classes/java/util/logging/LogManager.java:checkAccess`：内部用 LoggingPermission 做访问控制（无 SecurityManager 时为空操作）。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getName()`、`getActions()`（null→空串、空串保留）。
- **组合/边界**：`instanceof BasicPermission`；`equals` 同名 true / 异名 false / null false / 非 Permission false；`hashCode` 一致性；`implies` 同名 true / 异名 false；`toString` 含 name。
- **健壮性**：纯内存对象，无全局状态。
