# java.security.Policy 黑盒行为测试设计

## 1. API 功能
- 静态：`getPolicy()`/`setPolicy`（JDK-27 抛 UnsupportedOperationException）、`getInstance(type, params)`（JDK-27 无实现）、`UNSUPPORTED_EMPTY_COLLECTION`
- 实例：`getPermissions(CodeSource)`、`getPermissions(ProtectionDomain)`、`implies`、`refresh`；嵌套接口 `Policy.Parameters`

## 2. Java 用户如何使用
子类 PolicySpi 由 provider 框架装配；JDK-27 setPolicy 固定拒绝，getPolicy 返回默认空策略。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Policy.java`（JDK-24+ setPolicy 抛 UOE）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/javax/security/auth/login/Configuration/GetInstance.java`（同型 getInstance 框架用法）

## 4. 测试用例设计 + 自动化实现
- 正常：getPolicy 非空、implies 空 ProtectionDomain 为 false、UNSUPPORTED_EMPTY_COLLECTION 只读且不 implies
- 组合：自定义 Policy 子类的 getPermissions/implies/refresh 行为（直接调用）
- 异常：setPolicy 抛 UnsupportedOperationException；getInstance 任意类型抛 NoSuchAlgorithmException（JDK-27 无策略实现）

断言入口：`PolicyTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
