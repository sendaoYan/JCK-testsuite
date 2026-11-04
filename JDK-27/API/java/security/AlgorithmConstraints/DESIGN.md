# java.security.AlgorithmConstraints 黑盒行为测试设计

## 1. API 功能
- 抽象方法：`permits(Set<CryptoPrimitive>, String, AlgorithmParameters)`、`permits(Set<CryptoPrimitive>, Key)`（JDK-27 新增）、`permits(Set<CryptoPrimitive>, String, Key, AlgorithmParameters)`

## 2. Java 用户如何使用
TLS/证书路径场景由 `SSLParameters.setAlgorithmConstraints` / `PKIXParameters` 传入；用户通常实现该接口禁用弱算法。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/AlgorithmConstraints.java`（3 个 permits 重载）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/sun/security/tools/keytool/WeakAlg.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/jdk/security/jarsigner/JarWithOneNonDisabledDigestAlg.java`（禁用算法约束的行为验证）

## 4. 测试用例设计 + 自动化实现
- 白盒：反射确认 3 个抽象方法与 CryptoPrimitive.PUBLIC_KEY_ENCRYPTION 常量
- 黑盒：实现三方法返回固定值，按入参组合逐一断言 permits 语义（true/false 透传 + null 参数路径）

断言入口：`AlgorithmConstraintsTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
