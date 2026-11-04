# java.security.cert.CertPathBuilderResult 黑盒行为测试设计

## 1. API 功能
结果接口：getCertPath/getTrustAnchor/getPolicyTree/getPublicKey + clone。

## 2. Java 用户如何使用
用户从 build 返回并转型为 PKIXCertPathBuilderResult 取细节。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathBuilder/* 一律向下转型使用。

## 4. 测试用例设计 + 自动化实现
黑盒：最小实现 + JDK 返回的 PKIXCertPathBuilderResult 实现该接口（instanceof）；自定义实现各 getter 原样返回且 clone 可用。

断言入口：`CertPathBuilderResultTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
