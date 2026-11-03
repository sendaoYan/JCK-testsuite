# javax.xml.crypto.dsig.XMLSignatureException — 设计案

## 1. API 功能
javax.xml.crypto.dsig.XMLSignatureException 类。实例/声明方法：getCause, printStackTrace。

## 2. Java 用户如何使用
四类构造器 + getMessage/getCause/toString。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.XMLSignatureException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLSignatureExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLSignatureExceptionTest.testBlackBox()`）覆盖：消息、无参 null、Throwable 消息=cause.toString()、cause 引用、toString 前缀。。
