# java.security.spec.RSAPrivateKeySpec — 设计案

## 1. API 功能
RSA 私钥规范

2/3 参构造；getter 原引用；getParams 为 JDK 后加通道；无 equals 覆盖。
KeyFactory("RSA") 私钥交换基类（RSAPrivateCrtKeySpec 继承）。

## 2. 使用方式
modulus/privateExponent（+params）

## 3. 参考来源
- Oracle javadoc `java.security.spec.RSAPrivateKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
同引用；3 参 params 透传/null 保持；null 构造；两个构造器计数；KeyFactory 闭环：RSA-512 私钥→getKeySpec（modulus.bitLength==512）→generatePrivate equals。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
