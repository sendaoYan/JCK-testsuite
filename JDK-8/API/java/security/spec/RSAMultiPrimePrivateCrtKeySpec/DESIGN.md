# java.security.spec.RSAMultiPrimePrivateCrtKeySpec — 设计案

## 1. API 功能
RSA 多素数 CRT 私钥规范

8 个 BigInteger + RSAOtherPrimeInfo[]（+keyParams）；null 数组合法（getter null）、空数组→IAE "must not be empty"；ctor/getOtherPrimeInfo 双重防御拷贝。
多素数 RSA（PKCS#1 v2.1）私钥材料交换。

## 2. 使用方式
9/10 参构造 + otherPrimeInfo 数组

## 3. 参考来源
- Oracle javadoc `java.security.spec.RSAMultiPrimePrivateCrtKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
全 getter 逐项往返；ctor 拷贝/返回新数组/改返回值隔离；null vs 空数组分叉；10 参 keyParams 透传；extends RSAPrivateKeySpec→KeySpec；身份 equals。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
