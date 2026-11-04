# javax.crypto.interfaces.DHPublicKey — 测试设计

## 1. API 功能
DH 公钥接口（JDK 1.4 起），继承 `DHKey` + `java.security.PublicKey`。

- `BigInteger getY()`：公钥值 y = g^x mod p（1 <= y <= p-1）。
- `default DHParameterSpec getParams()`：默认实现返回 null，由实现类覆盖。
- 字段：`long serialVersionUID = -6628103563352519193L`（JDK 24 起 @Deprecated，仍在）。

## 2. Java 用户如何使用
实例来自 `KeyPairGenerator("DiffieHellman")` 的 `getPublic()`；公钥是对外分发的一侧，典型流向是交给对端 KeyAgreement、或以 X.509 编码/KeySpec 形式序列化传输：

```java
DHPublicKey pub = (DHPublicKey) kp.getPublic();
BigInteger y = pub.getY();                      // 对端可用 pub.getParams() 校验参数一致性
KeyFactory kf = KeyFactory.getInstance("DH");
DHPublicKeySpec spec = kf.getKeySpec(pub, DHPublicKeySpec.class);      // y/p/g
DHPublicKey fromEnc = (DHPublicKey) kf.generatePublic(new X509EncodedKeySpec(pub.getEncoded()));
DHPublicKey translated = (DHPublicKey) kf.translateKey(foreignKey);    // 外来密钥转提供者实现
kaPeer.init(peerPriv); kaPeer.doPhase(pub, true);                      // 对端消费本公钥
```

组合链路：`genKeyPair → getY 代数校验 → KeyFactory spec/X.509/translateKey 三路重建 → 对端协商对账 → 序列化往返`。

## 3. 开源代码实际用法调研
Web 搜索在本环境不可用（WebSearch 无结果返回），按规则改用 JDK 权威源码与自带测试（路径均在 `~/git/jdk25u-dev`）：

- `src/java.base/share/classes/com/sun/crypto/provider/DHPublicKey.java`（L246-283）：`getFormat()=="X.509"`、`getAlgorithm()=="DH"`、`getEncoded()` clone、equals 比较 y 与参数——断言的权威依据。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyAgreement2.java`（L149、L179-213）：`((DHPublicKey)alicePubKey).getParams()` 取参数；`aliceKeyAgree.doPhase(bobPubKey, true)` 双方互喂公钥后比对 `generateSecret`，并演示 `generateSecret(byte[], off)` 缓冲变体。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyFactory.java`（L80）：`kfac.generatePublic(x509KeySpec)` 由 X.509 编码重建公钥。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/SupportedDHKeys.java`（L76-81）：`(DHPublicKey)kp.getPublic()` 后 `publicKey.getParams().getP()/getG()` 用于重新初始化生成器。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/TestLeadingZeroes.java`：以 DH 公钥协商秘密的前导零场景，佐证 y 值/秘密只断关系不断字节。

## 4. 测试用例设计 + 自动化实现
`DHPublicKeyTest.java`，白盒 12 + 黑盒 22 断言。

白盒（testWhiteBox，保留原断言并补充）：
- public/interface/无父类、extends DHKey+java.security.PublicKey、方法 getY/getParams 存在（原有）；
- getY 返回 BigInteger；getParams 返回 DHParameterSpec 且为 default 方法；
- serialVersionUID 为 public static final long 且值 = -6628103563352519193L。

黑盒（testBlackBox，场景）：
1. 类型家族：公钥 instanceof DHPublicKey/DHKey/Key；getAlgorithm=="DH"、getFormat=="X.509"。
2. getY()：非 null、正、< p，且与同对私钥满足 y == g^x mod p（跨接口代数组合）。
3. getEncoded()：非空、两次调用内容相等且非同一数组（防御性拷贝）。
4. KeyFactory 三路重建：getKeySpec(DHPublicKeySpec) y/p/g 相等；generatePublic(spec) → getY 相等且 equals/hashCode 成立；generatePublic(X509EncodedKeySpec) → getY 与编码逐字节一致；translateKey → 返回 DHPublicKey 且 y 相等。
5. 对端消费：B 方用自己的私钥 + 本公钥协商，与 A 方用 B 公钥的结果一致；再用 spec 重建公钥协商仍一致。
6. 序列化往返（KeyRep）：getY 与 getEncoded 保持，且 equals 原始公钥。
7. equals 边界：不同密钥对的公钥不等。

确定性：密钥对随机，只断关系/往返/代数一致性；格式名、算法名、编码行为固定。

## 5. JDK-8 版本差异
JDK-8 版本差异：同 DHPrivateKey，getY 以外 getParams 继承自 DHKey；JDK-9+ 重复声明为 default 方法。
