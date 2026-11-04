# java.security.spec.InvalidKeySpecException — 设计案

## 1. API 功能
无效密钥规范异常

4 构造（()/String/Throwable/String+Throwable），继承 GeneralSecurityException，Serializable。
KeyFactory.generatePublic/generatePrivate/getKeySpec 在规范无效时抛出。

## 2. 使用方式
KeySpec 族受检异常

## 3. 参考来源
- Oracle javadoc `java.security.spec.InvalidKeySpecException`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
构造矩阵（(Throwable) message==cause.toString()）；initCause 一次性（二次→ISE；已有 cause→ISE）；addSuppressed/setStackTrace 生效；序列化往返保 message+cause；toString/getLocalizedMessage。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
