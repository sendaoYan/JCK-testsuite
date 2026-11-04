# java.security.spec.RSAKeyGenParameterSpec — 设计案

## 1. API 功能
RSA 密钥生成参数规范

2/3 参构造；常量 F0=3、F4=65537（BigInteger）；keysize 不做校验（负/0 均可，实际行为记录）；null 指数合法。
KeyPairGenerator.getInstance("RSA").initialize(spec)。

## 2. 使用方式
keysize+公钥指数（+keyParams）

## 3. 参考来源
- Oracle javadoc `java.security.spec.RSAKeyGenParameterSpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
常量精确值；构造边界（负 keysize 接受、null 指数）；keyParams 透传；身份 equals；引擎闭环：initialize(spec) 后生成 modulus.bitLength==512、指数==F4。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
