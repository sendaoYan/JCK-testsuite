# javax.crypto.KeyAgreement 设计案

## 1. API 功能
密钥协商引擎（final 包装类，保护构造器 `protected KeyAgreement(KeyAgreementSpi, Provider, String)`）：
- 工厂：`getInstance(String)` / `getInstance(String, String)` / `getInstance(String, Provider)`，均 final static。
- 属性：`getAlgorithm()`（返回创建时的算法名）、`getProvider()`。
- 初始化：`init(Key)`、`init(Key, SecureRandom)`、`init(Key, AlgorithmParameterSpec)`、`init(Key, AlgorithmParameterSpec, SecureRandom)`。
- 协商：`doPhase(Key, boolean lastPhase)` 执行一轮，返回中间结果（可为 null）。
- 秘密导出：`generateSecret()`（byte[]）、`generateSecret(byte[], int offset)`（写入并返回长度）、`generateSecret(String algorithm)`（包装为 SecretKey）。

## 2. Java 用户如何使用
典型 ECDH 两方流程：双方各自 `KeyPairGenerator.getInstance("EC").initialize(256)` 生成密钥对 → `KeyAgreement.getInstance("ECDH")` → `init(自己私有钥)` → `doPhase(对方公钥, true)` → `generateSecret()` 得到相同共享秘密。XDH 用 `"XDH"`/`"X25519"` + `NamedParameterSpec("X25519")`，X25519 秘密恒为 32 字节；ECDH P-256 秘密因前导零可能短于 32 字节，断言区间 [1,32] 且双方一致。秘密可经 `generateSecret("Generic")` 包装为 SecretKey（SunEC 只接受 "Generic"/"TlsPremasterSecret"，实测 "AES" 抛 NoSuchAlgorithmException）。状态机：init 前后 doPhase/generateSecret 均 `IllegalStateException`；生成后未再次 doPhase 再 generateSecret 也 `IllegalStateException`（与 JDK 自带测试一致）。

## 3. 开源代码实际用法调研
web 搜索在当前环境不可用，采用 JDK 源码与自带测试作为用法权威（均已核对路径与行为）：
- `~/git/jdk25u-dev/test/jdk/java/security/KeyAgreement/KeyAgreementTest.java` L125-152：`ka1.doPhase(kp2.getPublic(), true); byte[] secret1 = ka1.generateSecret();` 双方对照，且断言"generateSecret 之后未再 doPhase 前不能再生成秘密"（L144-146 期待异常）——本设计沿用该状态断言。
- `~/git/jdk25u-dev/test/jdk/com/sun/crypto/provider/KeyAgreement/DHKeyAgreement2.java` L128-163：DH 三方流程 `getInstance("DH", provider) → init(private) → doPhase(public, true)` 的经典用法。
- `~/git/jdk25u-dev/src/java.base/share/classes/sun/security/ec/ECDHKeyAgreement.java` L238-263：`engineGenerateSecret(byte[],int)` 先做容量检查（不足抛 `ShortBufferException` "Need N bytes"），`engineGenerateSecret(String)` 仅放行 `KeyUtil.isSupportedKeyAgreementOutputAlgorithm`（"TlsPremasterSecret"/"Generic"）——实测确认 "AES" 被 SunEC 拒绝。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/KeyAgreement.java`：包装类各 final 方法直接委托给 spi；工厂找不到算法抛 `NoSuchAlgorithmException`、provider 名不存在抛 `NoSuchProviderException`。

## 4. 测试用例设计 + 自动化实现
`KeyAgreementTest.java`：
- testWhiteBox：public 具体类、直接父类 Object、protected (Spi,Provider,String) 构造器、6 个公共方法存在、getAlgorithm 为 final。
- testBlackBox（场景分组）：
  - `ecdhTwoParty`：算法/提供者属性；init 前 doPhase / generateSecret / generateSecret(byte[],int) 三种 `IllegalStateException`；init-only 后 generateSecret `IllegalStateException`；双方秘密一致且长度 ∈[1,32]；消耗后再次 generateSecret `IllegalStateException`；offset 写入 + 缓冲区外字节不动 + 小缓冲 `ShortBufferException`；`generateSecret("Generic")` 返回算法名与字节一致的 SecretKey；不支持的秘密算法（"NoSuchSecretAlg" 与实测的 "AES"）抛 `NoSuchAlgorithmException`；XDH 私钥喂给 ECDH 抛 `InvalidKeyException`。
  - `xdhLegacyAndAlias`：保留旧浅测试的 X25519 单方流程断言；"XDH" + NamedParameterSpec 两方 32 字节一致；X25519 doPhase 返回 null。
  - `factoryAndErrors`：未知算法 `NoSuchAlgorithmException`（1/2/3 参重载）、未知 provider `NoSuchProviderException`、"SunEC" 具名与 Provider 对象重载的提供者选择。
