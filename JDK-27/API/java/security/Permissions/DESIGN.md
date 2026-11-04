# java.security.Permissions 黑盒行为测试设计

## 1. API 功能
- 操作：`add(Permission)`（含 UnresolvedPermission 解析）、`implies`、`elements`、`setReadOnly/isReadOnly`

## 2. Java 用户如何使用
保护域/策略的标准容器：add 分桶存放，implies 先解析 Unresolved 再判定。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Permissions.java`（add 时解析 UnresolvedPermission）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PermissionCollection/Concurrent.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/security/UnresolvedPermission/AccessorMethods.java`

## 4. 测试用例设计 + 自动化实现
- 正常：混合 add（Runtime/Property/Security/All），implies 矩阵（含通配）
- 组合：先 add UnresolvedPermission 再 implies 对应权限 → 解析为 true；不匹配 → false
- 边界：AllPermission 加入后 implies 一切；空集合不 implies 任何权限

断言入口：`PermissionsTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
