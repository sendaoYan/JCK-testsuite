# java.security.UnrecoverableKeyException 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(String)`

## 2. Java 用户如何使用
getKey(alias, wrongPassword) 失败的标准信号。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/UnrecoverableKeyException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/TestKeyStoreBasic.java`（错口令抛 UKE）

## 4. 测试用例设计 + 自动化实现
- 正常：2 构造器 message 精断、toString/getMessage 一致
- 组合：PKCS12 round-trip 后错口令 getKey 抛本异常、对 alias getEntry 抛 UnrecoverableEntryException 家族

断言入口：`UnrecoverableKeyExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
