# java.security.cert.CertificateException 黑盒行为测试设计

## 1. API 功能
证书体系根受检异常：ctor()/(String)/(String,Throwable)/(Throwable)。

## 2. Java 用户如何使用
CertificateFactory.generateCertificate 坏输入、checkValidity 失败、verify 失败等都抛出或以其子类抛出。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/ 全目录普遍 catch。

## 4. 测试用例设计 + 自动化实现
黑盒：四 ctor message/cause 语义；子类实例被 catch (CertificateException) 捕获；toString/序列化稳定；null message 时 toString 仍含类名。

断言入口：`CertificateExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
