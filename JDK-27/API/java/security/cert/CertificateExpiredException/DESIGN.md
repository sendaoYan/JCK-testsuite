# java.security.cert.CertificateExpiredException 黑盒行为测试设计

## 1. API 功能
证书过期异常（checkValidity(now)）：四 ctor 形态。

## 2. Java 用户如何使用
用户调用 X509Certificate.checkValidity() 于过期证书时捕获。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertificateFactory/ 与 X509Certificate 用例中过期证书路径。

## 4. 测试用例设计 + 自动化实现
黑盒：四 ctor 语义；用 FX root 证书的 notAfter（2036-10-31）之前的固定日期 2037-01-01 构造场景无法直接触发（fixture 未过期），故断言异常类型层次、message/cause 往返与序列化。

断言入口：`CertificateExpiredExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
