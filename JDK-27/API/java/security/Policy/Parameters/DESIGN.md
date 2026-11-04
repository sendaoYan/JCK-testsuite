# java.security.Policy.Parameters 黑盒行为测试设计

## 1. API 功能
- 无方法；`URIParameter` 是 JDK 中唯一实现

## 2. Java 用户如何使用
传给 `Policy.getInstance(type, params)` 标识参数来源。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Policy.java`（嵌套 interface Parameters）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/javax/security/auth/login/Configuration/GetInstance.java`（参数接口模式）

## 4. 测试用例设计 + 自动化实现
- 白盒：interface、无声明方法、URIParameter 实现之
- 黑盒：URIParameter 实例 instanceof Policy.Parameters；与 Policy.getInstance 组合走 NoSuchAlgorithmException 路径

断言入口：`ParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
