# java.security.cert.PolicyQualifierInfo 黑盒行为测试设计

## 1. API 功能
策略限定符：ctor(byte[] DER)、getPolicyQualifierId/getEncoded/getPolicyQualifier。

## 2. Java 用户如何使用
PolicyNode.getPolicyQualifiers 的元素类型。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/PolicyNode/GetPolicyQualifiers.java。

## 4. 测试用例设计 + 自动化实现
黑盒：手工 DER（SEQUENCE{OID 1.3.6.1.5.5.7.2.1, OCTET STRING "https://jck.example"}）解析出 CPS id 与限定符字节（含可读 URL）；getEncoded 非空；垃圾字节抛 IOException；两实例内容一致。

断言入口：`PolicyQualifierInfoTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
