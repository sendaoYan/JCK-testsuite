# java.security.spec.DSAPublicKeySpec — 设计案

## 1. API 功能
DSA 公钥规范

同 DSAPrivateKeySpec 结构，y 为公钥分量。
KeyFactory("DSA") getKeySpec/generatePublic 往返。

## 2. 使用方式
y/p/q/g 四参数容器

## 3. 参考来源
- Oracle javadoc `java.security.spec.DSAPublicKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
镜像私钥侧 + generatePublic equals 闭环。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
