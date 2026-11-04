# javax.crypto.EncryptedPrivateKeyInfo 设计案（JDK-8）

## 1. API 功能
PKCS#8 `EncryptedPrivateKeyInfo` ASN.1 类型的 Java 表现（public 具体非 final）：
- 构造：`EncryptedPrivateKeyInfo(byte[] encoded)`（解析 DER，IOException）；`(String algName, byte[] encryptedData)`（算法名须能解析为 OID，否则 NoSuchAlgorithmException；空数据 IllegalArgumentException、null NPE）；`(AlgorithmParameters, byte[])`。
- 查询：`getAlgName()`、`getAlgParameters()`、`getEncryptedData()`（防御拷贝）、`getEncoded()`（DER）。
- 解密取 spec：`getKeySpec(Cipher)`、`getKeySpec(Key)`、`getKeySpec(Key, String)`、`getKeySpec(Key, Provider)` → `PKCS8EncodedKeySpec`。
- 注意：JDK-8 没有 JDK-21+ 预览 API `encrypt/getKey/getKeyPair`，也没有 `java.security.BinaryEncodable`（JDK 23+）；本测试只覆盖 JDK-8 已有的构造器与 getKeySpec/getEncoded。

## 2. Java 用户如何使用
经典链路（PKCS#12 风格）：`SecretKeyFactory.getInstance("PBEWithSHA1AndDESede")` 由口令取 PBE key → `Cipher` 用 `PBEParameterSpec(salt, count)` 加密私钥的 PKCS8 编码 → `new EncryptedPrivateKeyInfo(cipher.getParameters(), ciphertext)` → `getEncoded()` 得到可写出的 DER；取回时 `getKeySpec(pbeKey)`（或传入已按 DECRYPT_MODE 初始化的 Cipher）还原 `PKCS8EncodedKeySpec`。`(String)` 构造器要求算法名可解析为 OID："AES"/"PBEWithMD5AndDES" 可用，"AES/GCM/NoPadding" 抛 NoSuchAlgorithmException；"DES" 作为裸算法名无法被 AlgorithmId 解析（JDK-8 实测 unrecognized algorithm name），AP 构造器路线因此用 `PBEWithMD5AndDES` 或 `PBEWithSHA1AndDESede`。

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/javax/crypto/EncryptedPrivateKeyInfo/` 下的 `GetKeySpec.java`、`GetAlgName.java`、`GetEncoded.java`、`GetKeySpecException.java`。
- 行为权威 `~/git/jdk8u-dev/jdk/src/share/classes/javax/crypto/EncryptedPrivateKeyInfo.java`（getAlgName = algid.getName()；getKeySpec 解密失败统一包成 `InvalidKeyException`）。

## 4. 测试用例设计 + 自动化实现
`EncryptedPrivateKeyInfoTest.java`：
- testWhiteBox：public 具体非 final、直接父类 Object、三个公共构造器精确签名、5 个方法名存在（getAlgName/getAlgParameters/getEncryptedData/getEncoded/getKeySpec）。
- testBlackBox：
  - `legacyAccessors`：保留旧浅测试断言（PBEWithMD5AndDES 名构造、getAlgName 非空、encData 长度、DER 非空、PBEWithMD5AndDES AlgorithmParameters 构造、getAlgParameters 非空）。
  - `aesCtorAndRoundTrip`：("AES",16B) → getAlgName "AES"、getAlgParameters null；输入/输出双向防御拷贝（改原数组不可见、两次 getEncryptedData 不同引用）；getEncoded 两次调用相等；DER 回构三相等（名/数据/再编码）。
  - `constructorErrors`：空数据 IAE、null 数据/null 算法名/null 编码 NPE、未知名与 "AES/GCM/NoPadding" NSAE、垃圾 DER IOException。
  - `pbeKeySpecRoute`：PBEWithSHA1AndDESede 全链路（AP 构造器 + getAlgName/getAlgParameters/getEncryptedData + getKeySpec 四重载 + DER 回构再解密 + 错口令 InvalidKeyException×2）。
