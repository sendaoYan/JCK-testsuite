# javax.xml.crypto.dsig.SignatureMethod — 设计案

## 1. API 功能
javax.xml.crypto.dsig.SignatureMethod 接口。常量：DSA_SHA1, DSA_SHA256, RSA_SHA1, RSA_SHA224, RSA_SHA256, RSA_SHA384, RSA_SHA512, SHA1_RSA_MGF1, SHA224_RSA_MGF1, SHA256_RSA_MGF1, SHA384_RSA_MGF1, SHA512_RSA_MGF1, ECDSA_SHA1, ECDSA_SHA224, ECDSA_SHA256, ECDSA_SHA384, ECDSA_SHA512, HMAC_SHA1, HMAC_SHA224, HMAC_SHA256, HMAC_SHA384, HMAC_SHA512, RSA_PSS, ED25519, ED448, SHA3_224_RSA_MGF1, SHA3_256_RSA_MGF1, SHA3_384_RSA_MGF1, SHA3_512_RSA_MGF1, ECDSA_SHA3_224, ECDSA_SHA3_256, ECDSA_SHA3_384, ECDSA_SHA3_512（共 33 个）。 实例/声明方法：getParameterSpec。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.crypto.dsig.SignatureMethod() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.SignatureMethod）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SignatureMethodTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SignatureMethodTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
