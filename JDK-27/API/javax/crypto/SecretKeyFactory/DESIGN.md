# javax.crypto.SecretKeyFactory 设计案

## 1. API 功能

对称密钥工厂（KeySpec ↔ SecretKey 双向转换），按方法分组：

- **工厂**：`getInstance(String)` / `getInstance(String, String providerName)` / `getInstance(String, Provider)`。
- **查询**：`getAlgorithm()`、`getProvider()`。
- **转换**：`generateSecret(KeySpec)`（KeySpec → SecretKey）、`getKeySpec(SecretKey, Class)`（SecretKey → 指定 KeySpec 类型）、`translateKey(SecretKey)`（外来 provider 的 key 转为本 provider key）。
- 异常：spec 类型/内容不合法 → `InvalidKeySpecException`；key 不可翻译 → `InvalidKeyException`；算法未注册 → `NoSuchAlgorithmException`。

SunJCE 在 JDK-27（Temurin）上可用算法：`DESede`、`PBEWithHmacSHA256AndAES_128`（PBES2，AES-128/CBC 内核）、`PBKDF2WithHmacSHA256` 等；`AES`/`ChaCha20` 未注册 SKF（实测 `NoSuchAlgorithmException`）。

## 2. Java 用户如何使用

- **对称原始密钥**：`SecretKeyFactory.getInstance("DESede")` → `generateSecret(new DESedeKeySpec(bytes))` 得 provider key；`getKeySpec(key, DESedeKeySpec.class)` 取回字节。
- **口令派生（PBKDF2）**：`new PBEKeySpec(password, salt, iterations, keyLength)` → `generateSecret(...).getEncoded()` 作 AES 密钥材料（Spring `Encryptors`、OWASP 推荐写法）。
- **PBE 加解密**：`generateSecret(new PBEKeySpec(pw, salt, iter))` 得 PBE key，配 `Cipher.getInstance("PBEWithHmacSHA256AndAES_128")` 与 `PBEParameterSpec(salt, iter)` 加密；解密需 `PBEParameterSpec(salt, iter, new IvParameterSpec(cipher.getIV()))`（实测纯 `IvParameterSpec`/无内层 IV 的 spec 会被 `InvalidAlgorithmParameterException` 拒绝）。

## 3. 开源代码实际用法调研

- OWASP Cryptographic Storage Cheat Sheet（[cheatsheetseries.owasp.org](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)）：PBKDF2-with-HMAC-SHA256 派生（固定 salt+迭代数）后 `SecretKeySpec` 用于 AES——对应黑盒 PBKDF2 段。
- Spring Security Crypto（[docs.spring.io](https://docs.spring.io/spring-security/reference/features-integrations/cryptography.html)）：`Encryptors.text(password, salt)` = PBKDF2(SHA-256) 派生 256 位 key + AES/GCM，与本测试"PBEKeySpec → getEncoded → AES 密钥"同构。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/SecretKeyFactory/SecKeyFacSunJCEPrf.java`（L60-70）：RFC 6070 PBKDF2-HMAC-SHA1 向量 `PBEKeySpec("password", "16-byte salt val", 4096, 160)` → `D2CACD3F1D44AF293C704F0B1005338D903C688C`——本测试作为 PBKDF2 机器的独立锚点直接采用。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/SecretKeyFactory/PBKDF2TranslateTest.java`：`generateSecret(PBEKeySpec)` → `getKeySpec(key, PBEKeySpec.class)` → `translateKey` 的往返链。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/SecretKeyFactory/SecKFTranslateTest.java`：DES/DESede `translateKey` 后 key 等价性比较。
- 行为源码：`~/git/jdk25u-dev/src/java.base/share/classes/com/sun/crypto/provider/PBKDF2KeyImpl.java`（`getFormat()=="RAW"`、getKeySpec 保留 iter/keyLength）；`com/sun/crypto/provider/PBEKey.java`（PBE key 的 `getEncoded` 为口令字节、`getFormat=="RAW"`——实测确认，任务书原假设 getEncoded==null 不成立）；`sun/security/util/PBEUtil.java` `getPBEKeySpec`（解密要求 PBE 参数中内嵌 IV）。

## 4. 测试用例设计 + 自动化实现

`SecretKeyFactoryTest.java`，`testWhiteBox()`（结构，14 check）：public 具体类、extends Object、protected 构造器 `(SecretKeyFactorySpi, Provider, String)`、方法名集合、static final 工厂、三个转换方法的精确返回类型。

`testBlackBox()`（行为，44 check）场景：

1. **DESede 往返**：奇校验 24 字节固定 key → `generateSecret(DESedeKeySpec)` → alg/format/长度/字节不变 → `getKeySpec` 回读字节相等 → `translateKey(SecretKeySpec("DESede"))` 保字节 → 与 DESede/CBC/PKCS5 加解密往返（17 字节明文 pad 到 24）→ `translateKey(工厂key)` 幂等。
2. **PBEWithHmacSHA256AndAES_128**：`getAlgorithm`/`getFormat=="RAW"`/`getEncoded`==口令 UTF-8 字节（SunJCE PBEKey 特性，实测）；同输入两次派生相等；派生 key 驱动同算法 Cipher 加密（11 字节 → 16 字节 ct，`getIV()` 16 字节）并用 `PBEParameterSpec(salt, iter, IvParameterSpec(iv))` 解密还原；错口令解密抛 `BadPaddingException`。
3. **PBKDF2WithHmacSHA256**：dkLen 256 → 32 字节；与测试内用 `Mac HmacSHA256`+`clone` 实现的 RFC 2898 PBKDF2 精确相等（交叉验证）；确定性；dkLen 128 → 16 字节；迭代数 4097 结果不同；`getKeySpec(key, PBEKeySpec.class)` 保留 iter=4096/keyLength=256；派生字节作 AES/GCM 密钥往返；RFC 6070 SHA-1 向量精确 hex 锚点。
4. **不可用算法**：`getInstance("AES")`/`("ChaCha20")` → `NoSuchAlgorithmException`。
5. **非法输入**：DESede 工厂收 `SecretKeySpec` → `InvalidKeySpecException`；`new DESedeKeySpec(8 字节)` → `InvalidKeyException`（构造期校验）；PBKDF2 `getKeySpec(非 PBE key)` → `InvalidKeySpecException`。
6. getInstance 三变体（含 SunJCE）与两类 provider 异常。
