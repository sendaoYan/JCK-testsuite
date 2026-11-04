# java.security.cert.CertPath.CertPathRep 黑盒行为测试设计

## 1. API 功能
CertPath 序列化代理：type+encoded，readResolve 还原证书链。

## 2. Java 用户如何使用
序列化 CertPath 时隐式触达。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk 序列化回归依赖其兼容性。

## 4. 测试用例设计 + 自动化实现
黑盒（经外层）：CertPath ObjectOutputStream 往返后 getCertificates().size() 与 getType 保持、与原链 equals。

断言入口：`CertPathRepTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
