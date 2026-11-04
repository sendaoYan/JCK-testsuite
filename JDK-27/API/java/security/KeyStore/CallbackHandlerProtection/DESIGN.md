# java.security.KeyStore.CallbackHandlerProtection 黑盒行为测试设计

## 1. API 功能
- 构造：`CallbackHandlerProtection(CallbackHandler)`；查询 `getCallbackHandler()`

## 2. Java 用户如何使用
无口令环境下由 CallbackHandler 交互式取密钥库口令，作为 ProtectionParameter 传给 Builder。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`（FileBuilder 对 CallbackHandlerProtection 重试 MAX_CALLBACK_TRIES 次）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/Builder/`（CallbackHandler 取口令路径）

## 4. 测试用例设计 + 自动化实现
- 正常：handler 同一实例往返、实现 ProtectionParameter
- 组合：真实 handler 服务 PasswordCallback 返回固定口令
- 异常：null handler → NPE

断言入口：`CallbackHandlerProtectionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
