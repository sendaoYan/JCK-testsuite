# javax.crypto.KeyAgreement 黑盒行为测试设计（JDK-8）

## 1. API 功能
- 工厂：`getInstance(algorithm)` / `getInstance(algorithm, providerName)` / `getInstance(algorithm, Provider)`。
- 初始化：`init(Key)` / `init(Key, SecureRandom)`（另有 `init(Key, AlgorithmParameterSpec[, SecureRandom])`）。
- 密钥协商：`doPhase(Key, boolean lastPhase)`、`generateSecret()` / `generateSecret(byte[], int offset)` / `generateSecret(String algorithm)`。
- 查询：`getAlgorithm` / `getProvider`。异常：`IllegalStateException`（未 init / 未 doPhase / secret 已消费）、`ShortBufferException`、`NoSuchAlgorithmException`、`NoSuchProviderException`、`InvalidKeyException`。

## 2. Java 用户如何使用
```java
KeyAgreement a = KeyAgreement.getInstance("DiffieHellman");
a.init(alicePrivate);
a.doPhase(bobPublic, true);
byte[] secret = a.generateSecret();
```

## 3. 参考来源
- Oracle Java SE 8 javadoc `javax/crypto/KeyAgreement.html`。
- OpenJDK 回归 `~/git/jdk8u-dev/jdk/test/javax/crypto/KeyAgreement`、`~/git/jdk/test/jdk/javax/crypto/KeyAgreement`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/javax_crypto/KeyAgreement`。

## 4. 测试用例设计
- DH 两方协商：init/doPhase/generateSecret，双方 secret 逐字节相同；未 init/未 doPhase/secret 已消费 → IllegalStateException；generateSecret(byte[],off) 偏移写入 + 返回长度 + ShortBufferException；generateSecret(String) 未知算法 → NoSuchAlgorithmException。
- ECDH（P-256）两方协商：双方 secret 相同；generateSecret(byte[],off) 偏移写入；JDK-8 SunEC 仅支持 TlsPremasterSecret，其余名字 → NoSuchAlgorithmException。
- 工厂异常：未知算法 → NoSuchAlgorithmException、未知 provider → NoSuchProviderException、SunJCE 无 ECDH → NoSuchAlgorithmException。
- 白盒：public 具体类、直接父类 Object、protected (KeyAgreementSpi, Provider, String) 构造器、六个方法名、getAlgorithm final。
- 注意：JDK-8 无 X25519/XDH/NamedParameterSpec（JDK-11+），故本测试用 DH + ECDH 两个标准算法，不绑定端口、不真握手。
