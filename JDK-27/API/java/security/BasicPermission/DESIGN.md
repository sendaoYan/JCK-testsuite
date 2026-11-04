# java.security.BasicPermission 黑盒行为测试设计

## 1. API 功能
- 构造：`BasicPermission(String)`、`BasicPermission(String, String)`（actions 忽略）
- 查询：`implies`（名字分层匹配 + `*`/`前缀.*` 通配）、`getActions()` 恒空串、`newPermissionCollection()`

## 2. Java 用户如何使用
子类（SecurityPermission 等）只给名字；`a.b.*` 蕴含 `a.b.c`，`*` 蕴含一切。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/BasicPermission.java`（JDK-27 只校验 null→NPE、空串→IAE）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PermissionCollection/Concurrent.java`、`AddToReadOnlyPermissionCollection.java`

## 4. 测试用例设计 + 自动化实现
- 正常：自反 equals/hashCode、getActions 空串、通配蕴含矩阵（`jck.*`→`jck.a`、`jck.a.b`；不蕴含 `jck`）
- 边界：JDK-27 接受 `jck..bad` 与尾点名字且不按通配解释
- 异常：null 名 NPE、空名 IAE

断言入口：`BasicPermissionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
