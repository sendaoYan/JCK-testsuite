# KeyPair — 设计案

## 1. API 功能
公/私钥对持有容器。构造器(PublicKey,PrivateKey) + getPublic/getPrivate。

## 2. 使用方式
`new KeyPair(pub, priv)`。

## 3. 参考来源
- javadoc `java.security.KeyPair`。
- JCK runtime-8b `api/java_security/KeyPair/`。

## 4. 测试用例设计 + 易错规范细节
- 生成 RSA 密钥对非空、格式 X.509/PKCS#8、getEncoded 非平凡。
- 构造器往返 equals；null 键合法（getPublic/getPrivate 存 null）；单侧 null 混合。
