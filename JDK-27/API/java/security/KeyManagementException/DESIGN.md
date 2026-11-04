# java.security.KeyManagementException 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`

## 2. Java 用户如何使用
密钥/证书管理操作失败的通用信号；JDK-27 中 Identity.addCertificate 等遗留 API 声明抛出。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyManagementException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/Identity/EqualsHashCodeContract.java`（Identity API 抛 KME）

## 4. 测试用例设计 + 自动化实现
- 正常：4 构造器 message/cause 精确断言（null 保持 null）
- 组合：catch 后 getCause/initCause 语义；toString/getMessage 一致
- 行为差异：JDK-27 Identity.setPublicKey 实际不再抛 KME（仅声明）

断言入口：`KeyManagementExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
