# java.security.cert.CertPathValidatorException.BasicReason 黑盒行为测试设计

## 1. API 功能
枚举：算法/未指定/无效签名/未过期/未生效/ revoked 等 7 常量（JDK-27 无 UNTRUSTED_CERT，实测确认）。

## 2. Java 用户如何使用
Reason 的标准取值。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/ 中与 getReason 断言搭配。

## 4. 测试用例设计 + 自动化实现
黑盒：values() 精确集合与顺序（排序后比较）、valueOf 往返、每个常量 instanceof Reason、可传入异常并读回。

断言入口：`BasicReasonTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
