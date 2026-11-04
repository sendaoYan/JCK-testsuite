# java.security.spec.KeySpec — 设计案

## 1. API 功能
密钥规范的标记接口

所有 *KeySpec 类型的公共标记接口；KeyFactory.generatePublic/generatePrivate/getKeySpec 以本接口体系交换密钥材料。
实现分两大族：EncodedKeySpec（PKCS8/X509 编码字节）与算法专有 spec（DSA*/RSA*/EC*）。

## 2. 使用方式
密钥材料透明承载接口

## 3. 参考来源
- Oracle javadoc `java.security.spec.KeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
标记语义验证（instanceof/isAssignableFrom 矩阵覆盖两族、与 AlgorithmParameterSpec 体系互斥、null 可赋值）。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
