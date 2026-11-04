# javax.crypto.interfaces.DHPrivateKey — 测试设计

## 1. API 功能
DH 私钥接口（JDK 1.4 起），继承 `DHKey` + `java.security.PrivateKey`（后者带来 `getAlgorithm/getFormat/getEncoded` 契约与可序列化性）。

- `BigInteger getX()`：私钥值 x（PKCS#3 §7.1 要求 1 <= x <= p-2 且 2^(L-1) <= x < 2^L）。
- `default DHParameterSpec getParams()`：默认实现返回 null，实际由实现类覆盖（SunJCE 的 `com.sun.crypto.provider.DHPrivateKey` 返回新构造的 spec）。
- 字段：`long serialVersionUID = 2211791113380396553L`（JDK 24 起 @Deprecated，仍在）。

## 2. Java 用户如何使用
实例来自 `KeyPairGenerator("DiffieHellman")`；随后可作为 KeyAgreement 的己方私钥、KeyFactory 的输入输出、或直接序列化传输：

```java
KeyPair kp = KeyPairGenerator.getInstance("DiffieHellman").generateKeyPair();
DHPrivateKey priv = (DHPrivateKey) kp.getPrivate();
BigInteger x = priv.getX();                     // 与 getParams().getP()/getG() 配套
KeyAgreement ka = KeyAgreement.getInstance("DiffieHellman");
ka.init(priv);                                  // 私钥进协商
ka.doPhase(peerPub, true);
byte[] secret = ka.generateSecret();            // 长度 = p.bitLength()/8
KeyFactory kf = KeyFactory.getInstance("DH");
DHPrivateKeySpec spec = kf.getKeySpec(priv, DHPrivateKeySpec.class);   // 导出 x/p/g
DHPrivateKey copy = (DHPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(priv.getEncoded()));
ObjectOutputStream oos = ...; oos.writeObject(priv);                    // KeyRep 序列化
```

组合链路：`genKeyPair → getX/getParams → y==g^x mod p 代数校验 → KeyFactory spec/编码双向往返 → KeyAgreement → 序列化往返再协商`。

## 3. 开源代码实际用法调研
Web 搜索在本环境不可用（WebSearch 无结果返回），按规则改用 JDK 权威源码与自带测试（路径均在 `~/git/jdk25u-dev`）：

- `src/java.base/share/classes/com/sun/crypto/provider/DHPrivateKey.java`（L262-301）：`getFormat()=="PKCS#8"`、`getAlgorithm()=="DH"`、`getEncoded()` 每次返回 clone、`getParams()` 当 l!=0 时返回 `new DHParameterSpec(p,g,l)`——本测试断言的权威依据。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyAgreement3.java`：官方序列化用例，把 DH 私钥/公钥经 ObjectOutputStream 往返后再跑 KeyAgreement。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyFactory.java`（L80-83）：`kfac.generatePublic(x509KeySpec)` / `kfac.generatePrivate(pkcsKeySpec)` 编码↔对象往返。
- `test/jdk/com/sun/crypto/provider/KeyAgreement/SupportedDHKeys.java`（L73-80）：`(DHPrivateKey)kp.getPrivate()` 后用 `getParams().getP()` 校验模数位数。
- 行为事实（本机 JDK-27 实测）：`kpg.initialize(512)` 使用预计算 safe-prime 参数（`ParameterCache`，g=2），`SecurityProviderConstants.getDefDHPrivateExpSize` 对 legacy 尺寸返回 L=160，故密钥 `getParams().getL()==160`。

## 4. 测试用例设计 + 自动化实现
`DHPrivateKeyTest.java`，白盒 12 + 黑盒 25 断言。

白盒（testWhiteBox，保留原断言并补充）：
- public/interface/无父类、extends DHKey+java.security.PrivateKey、方法 getX/getParams 存在（原有）；
- getX 返回 BigInteger；getParams 返回 DHParameterSpec 且为 default 方法；
- serialVersionUID 为 public static final long 且值 = 2211791113380396553L。

黑盒（testBlackBox，场景）：
1. 类型家族：私钥 instanceof DHPrivateKey/DHKey/Key；元数据 getAlgorithm=="DH"、getFormat=="PKCS#8"。
2. getX()：非 null、正（signum==1）、< p；同对公钥满足 y == g^x mod p（用 getX 做代数验证）。
3. getEncoded()：非空、两次调用内容相等且不是同一数组（防御性拷贝）。
4. getParams().getL()==160（512 位 safe-prime 预计算参数的 legacy 指数默认值，实测稳定）。
5. KeyFactory：getKeySpec(DHPrivateKeySpec) 的 x/p/g 与原 key 相等；generatePrivate(spec) 重建后 getX 相等、p/g 保持、equals 成立；注：spec 不携带 L，重建的 PKCS#8 字节与原编码不同（115 vs 119 B），byte 级一致走 PKCS8EncodedKeySpec 路径——两条路径分别断言。
6. KeyAgreement：init(priv)+doPhase(pub) → generateSecret 长度 == p.bitLength()/8。
7. 序列化往返（KeyRep）：getX/params/format 保持；用反序列化私钥再做一次协商，秘密与原始一致。
8. equals/hashCode：自身相等、hashCode 稳定、spec 重建实例 equals 且 hashCode 相等；不同密钥对不等。

确定性：密钥对随机，只断区间/关系/往返一致性；L、格式名、算法名、编码行为均为固定实现值。

## 5. JDK-8 版本差异
JDK-8 版本差异：getParams 仅在父接口 DHKey 上声明（JDK-9+ 才在本接口重复声明为 default 方法）；JDK-8 测试改为断言接口公共表面可见 getParams 且返回 DHParameterSpec。
