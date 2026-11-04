# java.security.spec.PKCS8EncodedKeySpec — 设计案

## 1. API 功能
PKCS#8 编码私钥规范

EncodedKeySpec 子类，getFormat 恒 "PKCS#8"；不解析内容（任意字节可存）。JDK-9+ 增加 (byte[],String) 构造器并实现 java.security.BinaryEncodable（预览标记）。
KeyFactory.generatePrivate 消费；PrivateKey.getEncoded() 产出。

## 2. 使用方式
私钥 DER 字节容器

## 3. 参考来源
- Oracle javadoc `java.security.spec.PKCS8EncodedKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
ctor null→NPE；空数组合法；ctor/getEncoded 防御拷贝与跨实例独立性；1KB 保真；身份 equals；KeyFactory 闭环：RSA 私钥 getEncoded→spec→generatePrivate equals 还原。JDK-27 追加 getAlgorithm 往返/null NPE/BinaryEncodable（反射检查，避免 --enable-preview）。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
