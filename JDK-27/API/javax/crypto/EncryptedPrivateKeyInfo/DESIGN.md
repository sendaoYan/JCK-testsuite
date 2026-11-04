# javax.crypto.EncryptedPrivateKeyInfo 设计案

## 1. API 功能
PKCS#8 `EncryptedPrivateKeyInfo` ASN.1 类型的 Java 表现（public，JDK-27 实现 `java.security.BinaryEncodable`）：
- 构造：`EncryptedPrivateKeyInfo(byte[] encoded)`（解析 DER，IOException）；`(String algName, byte[] encryptedData)`（算法名须能解析为 OID，否则 NoSuchAlgorithmException；空数据 IllegalArgumentException、null NPE）；`(AlgorithmParameters, byte[])`。
- 查询：`getAlgName()`、`getAlgParameters()`、`getEncryptedData()`（防御拷贝）、`getEncoded()`（DER）。
- 解密取 spec：`getKeySpec(Cipher)`、`getKeySpec(Key)`、`getKeySpec(Key, String)`、`getKeySpec(Key, Provider)` → `PKCS8EncodedKeySpec`。
- JDK-27 新增（预览）：`encrypt(BinaryEncodable, char[])`、`encrypt(BinaryEncodable, char[], String, AlgorithmParameterSpec, Provider)`、`encrypt(BinaryEncodable, Key, String, AlgorithmParameterSpec, Provider, SecureRandom)`；`getKey(char[])`/`getKey(Key)` → PrivateKey；`getKeyPair(char[])`/`getKeyPair(Key)` → KeyPair（KeyPair 自身实现 BinaryEncodable）。

## 2. Java 用户如何使用
经典链路（PKCS#12 风格）：`SecretKeyFactory.getInstance("PBEWithSHA1AndDESede")` 由口令取 PBE key → `Cipher` 用 `PBEParameterSpec(salt, count)` 加密私钥的 PKCS8 编码 → `new EncryptedPrivateKeyInfo(cipher.getParameters(), ciphertext)` → `getEncoded()` 得到可写出的 DER；取回时 `getKeySpec(pbeKey)`（或传入已按 DECRYPT_MODE 初始化的 Cipher）还原 `PKCS8EncodedKeySpec`。JDK-27 预览 API 一行完成：`EncryptedPrivateKeyInfo.encrypt(privateKey, password)`（默认 PBEWithHmacSHA256AndAES_128）→ `getKey(password)` 还原私钥、`encrypt(keyPair, password)` + `getKeyPair(password)` 同时找回公私钥（实测恢复的 RSA 私钥与原钥模数/私指数一致但 DER 编码可不同——生成器裁剪编码所致，故只断言数学材料）。注意：`(String)` 构造器要求算法名可解析为 OID，"AES" 可用而 "AES/GCM/NoPadding" 抛 NoSuchAlgorithmException；`PBEWithHmacSHA256AndAES_128` 的 AlgorithmParameters 不被 AlgorithmId 识别（实测 unrecognized algorithm name），AP 构造器路线因此用 `PBEWithSHA1AndDESede`。

## 3. 开源代码实际用法调研
web 搜索在当前环境不可用，采用 JDK 自带测试（均在 `~/git/jdk25u-dev/test/jdk/javax/crypto/EncryptedPrivateKeyInfo/`）：
- `GetKeySpec.java` L62-135：PBEWithSHA1AndDESede + `PBEParameterSpec(new byte[8], 1024)`、`AlgorithmParameters.getInstance(alg).init(spec)`、`new EncryptedPrivateKeyInfo(GOOD_PARAMS, encryptedData)`，随后 `getKeySpec(Cipher)`/`getKeySpec(Key)`/`getKeySpec(Key,String)`/`getKeySpec(Key,Provider)` 四重重载比对 `Arrays.equals(spec.getEncoded(), encodedKey)`——本设计第 3 节场景照搬此链路。
- `EncryptKey.java`（8298420，预览）：`encryptKey(priKey, password, algName, pbeSpec, provider)` 与 `getKey`/`getKeyPair` 往返，含错误口令异常路径——本设计预览场景与之一致（JDK-27 中方法名为 `encrypt`）。
- `GetAlgName.java`、`GetEncoded.java`：getAlgName 的 OID 名映射与 DER 稳定性。
- `GetKeySpecException.java`/`GetKeySpecException2.java`：非法解密参数/坏 DER 时构造器与 getKeySpec 的异常类型。
- 行为权威 `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/EncryptedPrivateKeyInfo.java` L246-247（getAlgName = algid.getName()）、L299-321（getKeySpecImpl：解密失败统一包成 `InvalidKeyException("Cannot retrieve the PKCS8EncodedKeySpec", ex)`）——错口令断言依据。

## 4. 测试用例设计 + 自动化实现
`EncryptedPrivateKeyInfoTest.java`（`@enablePreview`，因 encrypt/getKey/getKeyPair 为预览 API）：
- testWhiteBox：public 具体非 final、实现 BinaryEncodable、三个公共构造器精确签名、8 个方法名存在、encrypt 为 static。
- testBlackBox：
  - `legacyAccessors`：保留旧浅测试全部断言（DES 名构造、getAlgName 非空、encData 长度、DER 非空、DES AlgorithmParameters 构造、getAlgParameters 非空）。
  - `aesCtorAndRoundTrip`：("AES",16B) → getAlgName "AES"、getAlgParameters null；输入/输出双向防御拷贝（改原数组不可见、两次 getEncryptedData 不同引用）；getEncoded 两次调用相等；DER 回构三相等（名/数据/再编码）。
  - `constructorErrors`：空数据 IAE、null 数据/null 算法名/null 编码 NPE、未知名与 "AES/GCM/NoPadding" NSAE、垃圾 DER IOException。
  - `pbeKeySpecRoute`：PBEWithSHA1AndDESede 全链路（AP 构造器 + getAlgName/getAlgParameters/getEncryptedData + getKeySpec 四重载 + DER 回构再解密 + 错口令 InvalidKeyException×2）。
  - `previewEncryptRoute`：encrypt(PrivateKey,pw) 默认算法名与参数可读（PBEParameterSpec salt/count）；getKey(pw) 算法与编码逐字节还原；错口令 InvalidKeyException；DER 回构再解密；显式算法+固定 salt 的 encrypt；encrypt(KeyPair,pw)+getKeyPair(pw)（公钥编码相等、私钥模数/私指数相等、双 RSA 算法名、错口令 InvalidKeyException）；getKey(pbeSecretKey)。
