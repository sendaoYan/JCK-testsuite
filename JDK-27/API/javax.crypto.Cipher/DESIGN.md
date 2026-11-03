# javax.crypto.Cipher 黑盒行为测试设计

## 1. API 功能
- 常量：`ENCRYPT_MODE/DECRYPT_MODE/WRAP_MODE/UNWRAP_MODE`（1–4）、`PUBLIC_KEY/PRIVATE_KEY/SECRET_KEY`（1–3）。
- 工厂：`getInstance(transformation)` / `getInstance(String, String providerName)` / `getInstance(String, Provider)`。
- 初始化：`init(mode, Key[, SecureRandom | AlgorithmParameterSpec | AlgorithmParameters | Certificate])` 共 8 个重载。
- 数据处理：`update(byte[]…)` x4 + `update(ByteBuffer, ByteBuffer)`；`doFinal()` x7 形态（含输出缓冲/偏移/ByteBuffer）；AAD：`updateAAD(byte[]…/ByteBuffer)`（须在 update 之前调用）。
- 密钥封装：`wrap(Key)` / `unwrap(byte[], String, int)`。
- 查询：`getProvider/getAlgorithm/getBlockSize/getOutputSize/getIV/getParameters/getExemptionMechanism`；静态 `getMaxAllowedKeyLength/getMaxAllowedParameterSpec`。
- 异常：`NoSuchAlgorithmException/NoSuchPaddingException/NoSuchProviderException/InvalidKeyException/InvalidAlgorithmParameterException/IllegalStateException/IllegalBlockSizeException/BadPaddingException(AEADBadTagException)/ShortBufferException`。

## 2. Java 用户如何使用
```java
Cipher e = Cipher.getInstance("AES/GCM/NoPadding");
e.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
e.updateAAD(aad);                       // 可选，须在 update 前
byte[] ct = e.doFinal(plaintext);       // ct = pt + 16B tag
Cipher d = Cipher.getInstance("AES/GCM/NoPadding");
d.init(Cipher.DECRYPT_MODE, key, gcmSpec);
byte[] pt = d.doFinal(ct);              // 篡改/错 key -> AEADBadTagException
```
CBC 分块链路：`init -> update(4B) -> update(4B) -> update(8B) -> update(16B) -> doFinal()`，与一次性 `doFinal` 输出逐字节一致。密钥封装链路：RSA `init(WRAP_MODE, pub) -> wrap(aesKey) -> init(UNWRAP_MODE, priv) -> unwrap`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/CipherStreamClose.java` L90–126：`Cipher.getInstance("AES/ECB/PKCS5Padding") -> init -> COS/CIS` 流式加解密与一次性结果比对（本测试的"CBC 流式 == one-shot"同型）。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/GCMAPI.java` L54–69：`updateAAD` 参数校验（null/越界拒绝、ByteBuffer 形态）。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/GetMaxAllowedKeyLength.java`（同目录 GetMaxAllowed）：无限制策略下 `getMaxAllowedKeyLength("AES")==Integer.MAX_VALUE`。
- 行为权威：`~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/Cipher.java`（getOutputSize L1052：未 init 抛 IllegalStateException、负数抛 IllegalArgumentException；getAlgorithm 返回 transformation）。
- Spring Security `spring-security-crypto` 的 `AesBytesEncryptor`/`Encryptors`（web 调研）：AES/GCM/NoPadding + GCMParameterSpec(128, 12B IV) 的标准用法（[Spring Security Crypto 文档](https://docs.spring.io/spring-security/reference/features-integrations/cryptography.html)）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`CipherTest.testBlackBox`，固定 16B key/12B GCM IV/16B CBC IV/5B+32B+37B 明文）：
- GCM：5B/37B（跨块）加密 ct 长度==pt+16、两实例同 key+IV 密文逐字节相同（确定性）、解密精确还原；AAD（byte[] 与 ByteBuffer）往返；update 后 updateAAD 抛 IllegalStateException；分块 20+12 == one-shot；同 key+IV 二次 init 加密抛 InvalidAlgorithmParameterException（SunJCE IV 复用防护）。
- 篡改密文首字节 / 错 key 解密 → AEADBadTagException；getOutputSize：加密 n+16、解密==pt 长度、未 init → IllegalStateException、-1 → IllegalArgumentException。
- CBC/NoPadding：块大小 16、getIV==init IV、getParameters 非空（AES）；分块 4+4+8+16 == one-shot；update(4B) 返回 0B、补满 16B 后吐出 16B（缓冲语义）；分块解密还原；同 IV 重新 init 确定性；非整块明文 → IllegalBlockSizeException；ByteBuffer doFinal 与数组 doFinal 一致；doFinal 输出缓冲偏移写入；update+doFinal 写同一缓冲。
- 默认转换 "AES"（=AES/ECB/PKCS5Padding）：getIV/getParameters 为 null、5B→16B。
- 工厂异常：未知算法 → NoSuchAlgorithmException；"AES/CBC/NoSuchPad" 实测抛 NoSuchAlgorithmException（转换查找先失败，任务书猜测的 NoSuchPaddingException 不成立，按实测断类型）；未知 provider 名 → NoSuchProviderException；20B AES key → InvalidKeyException；输出缓冲过小 → ShortBufferException。
- RSA-2048 OAEP wrap AES key → 256B；unwrap 编码、算法、SecretKey 类型三重相等。
- getMaxAllowedKeyLength("AES")==Integer.MAX_VALUE（无限制策略）。
白盒场景（`testWhiteBox`）：public 非 final、直接父类 Object、protected 构造器、7 个常量 static final int、17 个方法名存在、getInstance x3/update x5/doFinal x7/init x8 重载计数。
实测校准：JDK-27 Temurin 27.0.1 上全部行为经探针程序逐一验证（含 NoSuchAlgorithmException 的 padding 路径与 GCM IV 复用防护）。
