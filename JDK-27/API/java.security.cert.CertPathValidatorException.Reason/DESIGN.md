# java.security.cert.CertPathValidatorException.Reason 黑盒行为测试设计

## 1. API 功能
Reason 标记接口（Serializable）。

## 2. Java 用户如何使用
用户实现自定义原因类型。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidatorException/（如有）与 Reason 用法。

## 4. 测试用例设计 + 自动化实现
黑盒：自定义 Reason 类传入异常 getReason 返回同对象；BasicReason/CRLReason/PKIXReason 均实现该接口（instanceof 断言）。

断言入口：`ReasonTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
