# java.security.spec.X509EncodedKeySpec — 设计案

## 1. API 功能
X.509 编码公钥规范

EncodedKeySpec 子类，getFormat 恒 "X.509"；不解析内容。JDK-9+ 增加 (byte[],String) 构造器并实现 BinaryEncodable。
KeyFactory.generatePublic 消费；PublicKey.getEncoded() 产出。

## 2. 使用方式
公钥 DER 字节容器

## 3. 参考来源
- Oracle javadoc `java.security.spec.X509EncodedKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
同 PKCS8 侧镜像 + 与 PKCS8 永不相等（格式通道区分）；RSA 公钥 getEncoded→spec→generatePublic equals 闭环。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
