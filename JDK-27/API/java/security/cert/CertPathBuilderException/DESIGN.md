# java.security.cert.CertPathBuilderException 黑盒行为测试设计

## 1. API 功能
构建失败受检异常：四 ctor。

## 2. Java 用户如何使用
build 无法成链时抛出/被捕获。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathBuilder/ 失败用例。

## 4. 测试用例设计 + 自动化实现
黑盒：四 ctor message/cause；对无 anchor 匹配的 target 触发真实异常路径并断言类型与 message 非空。

断言入口：`CertPathBuilderExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
