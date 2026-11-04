# java.security.UnresolvedPermission 黑盒行为测试设计

## 1. API 功能
- 构造：`UnresolvedPermission(type, name, actions, certs)`；查询 `getUnresolvedType/Name/Actions/Certificates`（getActions 恒空）

## 2. Java 用户如何使用
权限类当时不可用时占位；Permissions.implies 命中时解析为真实权限。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/UnresolvedPermission.java`、`/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Permissions.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/UnresolvedPermission/AccessorMethods.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PermissionCollection/Concurrent.java`

## 4. 测试用例设计 + 自动化实现
- 正常：4 个 getUnresolved* 精确透传、自身 implies 任何权限为 false、getActions 空
- 组合：add 进 Permissions 后 implies 匹配权限 → true（解析发生）、不匹配 → false
- 边界：null certs → getUnresolvedCertificates null

断言入口：`UnresolvedPermissionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
