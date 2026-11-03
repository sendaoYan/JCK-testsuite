# java.security.cert.PKIXRevocationChecker.Option 黑盒行为测试设计

## 1. API 功能
吊销选项枚举：ONLY_END_ENTITY/PREFER_CRLS/NO_FALLBACK/SOFT_FAIL 四常量。

## 2. Java 用户如何使用
setOptions 的取值域。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/OCSP/*。

## 4. 测试用例设计 + 自动化实现
黑盒：values() 精确 4 常量（排序比较）、valueOf 往返、EnumSet.allOf 设入后读回 4 个、单选读回一致。

断言入口：`OptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
