# javax.crypto.spec.HPKEParameterSpec 设计案

## 1. API 功能
HPKE（RFC 9180）参数规格（JDK 27 新增，`public final class implements java.security.spec.AlgorithmParameterSpec`，私有构造）：
- 常量：5 个 KEM id（P-256/P-384/P-521/X25519/X448 = 0x10/0x11/0x12/0x20/0x21）、3 个 KDF id（HKDF-SHA256/384/512 = 0x1/0x2/0x3）、3 个 AEAD id（AES-128/256-GCM、ChaCha20-Poly1305 = 0x1/0x2/0x3）与 EXPORT_ONLY = 0xffff。
- 静态工厂：`of(int kem_id, int kdf_id, int aead_id)` —— mode_base、空 info/psk_id；各 id 超出 [0,65535] → IllegalArgumentException。
- 不可变 wither：`withInfo(byte[])`（null NPE、空 IAE、克隆）、`withPsk(SecretKey, byte[] psk_id)`（null NPE×2、psk_id 空 IAE、RAW 且 <32 字节 IAE、psk 保持引用、psk_id 克隆）、`withEncapsulation(byte[])`（null NPE、克隆）、`withAuthKey(AsymmetricKey)`（null NPE、保持引用）；均返回新对象不改原对象。
- 查询：`kem_id()/kdf_id()/aead_id()/info()/psk()/psk_id()/authKey()/encapsulation()`（数组 getter 均克隆，info/psk_id 永非 null 可为空）。
- `toString()`：`HPKEParameterSpec{kem_id=…, kdf_id=…, aead_id=…, info=…, mode_base|mode_psk|mode_auth|mode_auth_psk}`，info 为 hex（可打印 ASCII 附加双格式）。

## 2. Java 用户如何使用
发送方 `Cipher.getInstance("HPKE").init(ENCRYPT_MODE, recvPublicKey, HPKEParameterSpec.of(kem,kdf,aead).withInfo(info))` → `getIV()` 取封装密文 encapsulation → `doFinal(pt)`；接收方 `of(...).withInfo(info).withEncapsulation(enc)` + `init(DECRYPT_MODE, recvPrivateKey, spec)` → `doFinal(ct)`。认证模式发送方 withAuthKey(发送方私钥)、接收方 withAuthKey(发送方公钥)；EXPORT_ONLY 只允许 export。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- JDK-27 `src.zip: java.base/javax/crypto/spec/HPKEParameterSpec.java`（jdk25u-dev 尚无此类）：of() 范围校验文案 "Invalid kem_id: …"、"info is empty"、"psk is too short"；withPsk 对 `"RAW".equalsIgnoreCase(psk.getFormat())` 才检查长度；toString 的 bytesToString 可打印 ASCII 双格式。
- JDK-27 `src.zip: java.base/com/sun/crypto/provider/HPKE.java`（CipherSpi 实现）：`engineInit` 强制要求 HPKEParameterSpec（无 spec → InvalidKeyException）、发送方 authKey 需 PrivateKey、接收方需 PublicKey 且必须 withEncapsulation、aead_id=EXPORT_ONLY 时 doFinal → UnsupportedOperationException、getIV() 返回 kemEncaps。
- 实测：`Cipher.getInstance("HPKE")` 在 SunJCE 可用；X25519 KEM encapsulation 32 字节；AEAD 密文 = 明文 + 16 字节 tag；错误 info 解密 → AEADBadTagException。
- `test/jdk/com/sun/crypto/provider/KDF/` 同族测试风格（RFC 向量精确断言）作为本测试参照。

## 4. 测试用例设计 + 自动化实现
`HPKEParameterSpecTest.java` 白盒（17 check）：public final/extends Object/implements AlgorithmParameterSpec、14 个方法名、无公共构造器、11 个常量精确值。黑盒（47 check）场景：
1. 保留旧断言：KEM id 互异、of() 三 getter、withInfo、toString 非空、instanceof。
2. of() 默认态：info 空、psk null、psk_id 空、encapsulation null、authKey null；边界 0/65535 接受；-1/65536/-2 → IAE。
3. withInfo：克隆、原对象不变、id 保持；null NPE、空 IAE。
4. withPsk：31 字节 IAE、null×2 NPE、psk_id 空 IAE、psk_id 克隆、psk 同一引用、原对象不变。
5. withEncapsulation/withAuthKey：null NPE、克隆/引用保持。
6. toString 精确断言：mode_base 全串、mode_psk/mode_auth 结尾、ASCII info（`6162 ("ab")`）与二进制 info（`00`）。
7. 真实链路（SunJCE "HPKE" Cipher）：base 模式完整往返（encapsulation 32B、密文=明文+16B tag、解密还原）；错误 info → AEADBadTagException；psk 模式往返；auth 模式（发送私钥/接收公钥）往返；EXPORT_ONLY：encapsulation 仍 32B、doFinal → UnsupportedOperationException；init 无 spec → InvalidKeyException。
