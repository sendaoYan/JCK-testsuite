# java.security.SecurityPermission 黑盒行为测试设计

## 1. API 功能
- 构造：`SecurityPermission(String)`、`SecurityPermission(String, String)`；语义继承 BasicPermission

## 2. Java 用户如何使用
控制 Security/Policy/Provider 配置类操作（"getPolicy"/"setPolicy"/"insertProvider" 等）。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/SecurityPermission.java`（deprecated for removal）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/io/FilePermission/FilePermissionCollection.java`（与同类 BasicPermission 家族组合）

## 4. 测试用例设计 + 自动化实现
- 正常：getName 透传、getActions 空、implies 层级/通配矩阵
- 边界：JDK-27 接受尾点名（不按通配解释）、`a..b` 名字同理
- 异常：null NPE、空串 IAE

断言入口：`SecurityPermissionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
