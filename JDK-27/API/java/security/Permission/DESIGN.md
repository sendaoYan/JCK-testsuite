# java.security.Permission 黑盒行为测试设计

## 1. API 功能
- 构造：`Permission(String name)`
- 抽象/核心：`implies`、`equals`/`hashCode`、`getActions`、`newPermissionCollection`、`checkGuard(Object)`、`getName`、`toString`

## 2. Java 用户如何使用
所有权限的根；checkGuard 在 JDK-24+ 固定抛 SecurityException（"checking permissions is not supported"）。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Permission.java`、`Guard.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PermissionCollection/Concurrent.java`

## 4. 测试用例设计 + 自动化实现
- 正常：getName 精确透传、toString 含类名与名字
- 组合：checkGuard(GuardedObject 场景) 抛 SecurityException；newPermissionCollection 默认 null
- 异常：null 名 NPE

断言入口：`PermissionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
