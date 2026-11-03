# java.security.Signer 黑盒行为测试设计

## 1. API 功能
- 构造：`Signer(String)`、`Signer(String, IdentityScope)`（protected）
- 操作：`setKeyPair(KeyPair)`、`getPrivateKey()`（继承 Identity 的密钥语义）

## 2. Java 用户如何使用
Identity 子类，可持私钥；JDK-27 setKeyPair 为静默替换，null 抛 NPE/InvalidParameterException。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Signer.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/util/jar/JarInputStream/ScanSignedJar.java`（签名主体场景）

## 4. 测试用例设计 + 自动化实现
- 正常：构造、getPrivateKey/setKeyPair 往返、publicKey 同步
- 组合：同一对重新 set 成功；不同 KeyPair 静默替换（JDK-27）
- 异常：setKeyPair(null) 拒绝；toString 含名字

断言入口：`SignerTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
