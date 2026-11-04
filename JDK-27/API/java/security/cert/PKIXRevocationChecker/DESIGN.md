# java.security.cert.PKIXRevocationChecker 黑盒行为测试设计

## 1. API 功能
吊销检查器：setOcspResponder/URI、setOcspResponderCert、setOcspExtensions、setOcspResponses(Map)、setOptions(Set<Option>) + getter + 抽象 getSoftFailExceptions + covariant clone。

## 2. Java 用户如何使用
用户向 PKIXParameters 注册以改走 OCSP/软失败。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/OCSP/OCSPTimeout.java、GetAndPostTests.java。

## 4. 测试用例设计 + 自动化实现
黑盒：CertPathValidator.getInstance("PKIX").getRevocationChecker() 得到实例；默认全空；URI/证书/扩展/字节表/选项全部往返；clone 独立；init(false) 可调用；是 PKIXCertPathChecker。

断言入口：`PKIXRevocationCheckerTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
