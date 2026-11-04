# javax.crypto.KeyGenerator 设计案

## 1. API 功能

对称密钥生成引擎，按方法分组：

- **工厂**：`getInstance(String)` / `getInstance(String, String providerName)` / `getInstance(String, Provider)`。
- **查询**：`getAlgorithm()`、`getProvider()`。
- **初始化（4+1 变体）**：`init(int keysize)` / `init(int, SecureRandom)`、`init(SecureRandom)`（仅换随机源）、`init(AlgorithmParameterSpec)` / `init(AlgorithmParameterSpec, SecureRandom)`；非法 keysize 抛 `InvalidParameterException`。
- **生成**：`generateKey()` → `SecretKey`（`getAlgorithm`/`getFormat=="RAW"`/`getEncoded` 为原始密钥字节）。不 init 时用 Provider 默认长度。

## 2. Java 用户如何使用

典型链路：`KeyGenerator.getInstance("AES")` → `init(256)` → `generateKey()`，再把 SecretKey 交给 `Cipher.getInstance("AES/GCM/NoPadding")` 加解密；或把 `getEncoded()` 持久化后 `new SecretKeySpec(bytes, "AES")` 重建。生成 HMAC 密钥：`getInstance("HmacSHA256").generateKey()` 直接得到 32 字节密钥供 `Mac` 使用。注意 `SecureRandom` 参数只影响随机源，不承诺同种子复现（SunJCE 的 AES 生成不走调用方 SecureRandom 逐字节路径，实测同种子两次生成不同 key，故测试只断言可用性与互异）。

## 3. 开源代码实际用法调研

- Baeldung《Java AES Encryption and Decryption》（[baeldung.com/java-aes-encryption-decryption](https://www.baeldung.com/java-aes-encryption-decryption)）：`KeyGenerator keyGen = KeyGenerator.getInstance("AES"); keyGen.init(256); SecretKey key = keyGen.generateKey();`，再配合 `AES/GCM/NoPadding`——本测试黑盒第 1-4 段同链路。
- Mkyong《Java AES Encryption and Decryption》（[mkyong.com/java/java-aes-encryption-and-decryption](https://mkyong.com/java/java-aes-encryption-and-decryption/)）：`keyGen.init(128)` 生成 16 字节 key 并打印 `key.getEncoded()` 长度。
- Spring Security Crypto 模块（[docs.spring.io](https://docs.spring.io/spring-security/reference/features-integrations/cryptography.html)）：`Encryptors.text(password, salt)` 内部用 PBKDF2 派生 + `KeyGenerator` 生成 AES 密钥（`BytesEncryptor`），推荐 GCM/CBC。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/KeyGenerator/CompareKeys.java`：遍历 Provider 注册的全部 KeyGenerator 算法，`generateKey()` 后比较 key 等价性——证明"生成 → getEncoded → 重建"是 JDK 官方认可用法。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/KeyGenerator/TestKGParity.java`：对生成 key 直接做奇偶位调整，说明 RAW 编码即密钥原始字节。
- 行为源码：`~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/KeyGenerator.java` L511/526（keysize 非法时 `InvalidParameterException`）；`~/git/jdk8u-dev/src/java.base/share/classes/com/sun/crypto/provider/AESKeyGenerator.java` L96（"Wrong keysize: must be equal to 128, 192 or 256"）。

## 4. 测试用例设计 + 自动化实现

`KeyGeneratorTest.java`，`testWhiteBox()`（结构，13 check）：public 具体类、extends Object、protected 构造器 `(KeyGeneratorSpi, Provider, String)`、方法名集合、static final 工厂、`generateKey` 返回 `SecretKey`、5 个 init 重载。

`testBlackBox()`（行为，35 check）场景：

1. AES-128：getAlgorithm/getProvider、key `getAlgorithm=="AES"`、`getFormat=="RAW"`、`getEncoded` 16 字节、instanceof SecretKey。
2. AES-256（unlimited 策略，Temurin 27 已验证）：32 字节。
3. 两次 `generateKey()` 互异（字节数组不等）；两把 key 各自完成 AES/GCM 128-bit tag 加解密往返；256 位 key 同样往返（证明生成 key 可用）。
4. 非法 keysize：`init(129)`、`init(0)`、`init(-5)` 均 `InvalidParameterException`。
5. HmacSHA256 KeyGenerator：默认 256 位 → 32 字节、format RAW，且能直接喂给 `Mac` 得 32 字节 tag。
6. `init(int, SecureRandom)` 生效；仅 `init(SecureRandom)` 也可生成可用 key。
7. 不 init 直接 `generateKey()`：默认长度属 {16,24,32}（SunJCE 默认随平台为 256/128，故断言合法集合而非具体值）。
8. getInstance 三变体（含 SunJCE 断言）；bogus 算法→`NoSuchAlgorithmException`、bogus provider→`NoSuchProviderException`、null→`NullPointerException`。
9. `new SecretKeySpec(generated.getEncoded(), "AES")` 重建等价且可往返。
