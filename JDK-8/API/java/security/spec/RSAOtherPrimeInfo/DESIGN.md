# java.security.spec.RSAOtherPrimeInfo — 设计案

## 1. API 功能
RSA 多素数 CRT 第三素数信息

3 参构造，任一 null→NPE（消息分别含 prime/primeExponent/crtCoefficient）；无接口、单构造器。
RSAMultiPrimePrivateCrtKeySpec 的 otherPrimeInfo 数组元素。

## 2. 使用方式
prime/exponent/crtCoefficient 三元组

## 3. 参考来源
- Oracle javadoc `java.security.spec.RSAOtherPrimeInfo`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
null 矩阵精确消息；getter 同引用；distinct 值往返；身份 equals；非 Serializable。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
