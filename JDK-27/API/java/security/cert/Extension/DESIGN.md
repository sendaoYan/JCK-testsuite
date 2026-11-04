# java.security.cert.Extension 黑盒行为测试设计

## 1. API 功能
扩展接口（java.security.cert 版）：getId/isCritical/getValue/encode(OutputStream)。

## 2. Java 用户如何使用
CertificateRevokedException.getExtensions 的元素类型；PKCS12 属性等实现（JDK-27 中 PKCS12Attribute 不实现它，实测确认）。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertificateRevokedException/Basic.java（Extension 子类传入）。

## 4. 测试用例设计 + 自动化实现
黑盒：自定义 FixedExtension 的四个方法契约（getId/isCritical/getValue 原样、encode 写出 value 字节）；作为 CRE extensions map 的值被 getExtensions 原样返回；子类多态 instanceof。

断言入口：`ExtensionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
