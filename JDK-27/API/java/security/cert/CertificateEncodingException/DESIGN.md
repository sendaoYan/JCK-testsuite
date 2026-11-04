# java.security.cert.CertificateEncodingException 黑盒行为测试设计

## 1. API 功能
编码失败的受检异常：ctor()/(String)/(String,Throwable)/(Throwable)。

## 2. Java 用户如何使用
用户在 Certificate.getEncoded、KeyStore 存证书等编码失败路径捕获。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk 多处 catch (CertificateEncodingException)（如 test/jdk/java/security/cert/ 下坏证书用例）。

## 4. 测试用例设计 + 自动化实现
黑盒：四个 ctor 的 message/cause 往返（null message 允许）、toString 含类名、getCause 为 null 或指定对象、序列化保持 message；作为 CertificateException 子类可被父类 catch。

断言入口：`CertificateEncodingExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
