# java.security.spec.RSAPublicKeySpec — 设计案

## 1. API 功能
RSA 公钥规范

镜像私钥侧；与 RSAPrivateKeySpec 为兄弟类（互不赋值）。
KeyFactory("RSA") 公钥交换。

## 2. 使用方式
modulus/publicExponent（+params）

## 3. 参考来源
- Oracle javadoc `java.security.spec.RSAPublicKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
同引用；params 透传；KeyFactory 闭环：公钥→getKeySpec（指数 65537）→generatePublic equals。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
