# java.security.spec.RSAPrivateCrtKeySpec — 设计案

## 1. API 功能
RSA CRT 私钥规范

modulus/publicExponent/privateExponent/primeP/primeQ/primeExponentP/primeExponentQ/crtCoefficient（+keyParams）；全 null 合法。
KeyFactory("RSA") CRT 私钥交换。

## 2. 使用方式
8/9 参构造

## 3. 参考来源
- Oracle javadoc `java.security.spec.RSAPrivateCrtKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
8 getter 逐项往返 + 同引用；8 参→getParams null、9 参→透传；null 构造逐字段；身份 equals；非 Serializable。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
