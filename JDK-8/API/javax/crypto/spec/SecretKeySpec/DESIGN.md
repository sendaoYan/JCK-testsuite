# javax.crypto.spec.SecretKeySpec 设计案

## 1. API 功能

与 provider 无关的原始字节密钥规格/密钥，`implements KeySpec, SecretKey`：

- **构造器**：`SecretKeySpec(byte[] key, String algorithm)`（整体复制）、`SecretKeySpec(byte[] key, int offset, int len, String algorithm)`（窗口复制）。二者均做防御拷贝并校验：null key/null algorithm/空 key → `IllegalArgumentException("Missing argument"/"Empty key")`；负 offset/负 len → `ArrayIndexOutOfBoundsException`；`offset+len > key.length` → `IllegalArgumentException("Invalid offset/length combination")`。
- **Key 访问器**：`getAlgorithm()`（原样返回构造名，不规范化）、`getFormat()=="RAW"`、`getEncoded()`（每次返回新拷贝）。
- **等价性**：`equals(Object)`——另一方须是 `SecretKey`，算法名忽略大小写（`DESede` 与 `TripleDES` 互认），字节做常数时间比较（`MessageDigest.isEqual`）；`hashCode()` 与 equals 一致（算法名忽略大小写参与散列）。
- 包内方法 `clear()`（零填充内部数组，非公共 API，javap 可见）。

## 2. Java 用户如何使用

最常见密钥载体：拿到原始字节（`KeyGenerator.generateKey().getEncoded()`、PBKDF2 派生字节、配置文件 hex）后 `new SecretKeySpec(bytes, "AES")` 直接交给 `Cipher`/`Mac`；窗口构造器用于从更大缓冲中取子段（如 PBES1 派生 key+iv 的场景）。等价性用于密钥指纹/去重比较。安全注意：构造器复制入参数组、`getEncoded()` 每次返回新数组，调用方应各自清零。

## 3. 开源代码实际用法调研

- Baeldung AES 指南（[baeldung.com/java-aes-encryption-decryption](https://www.baeldung.com/java-aes-encryption-decryption)）：`new SecretKeySpec(keyBytes, "AES")` 由密码/字节重建密钥再 `Cipher.init`——对应黑盒 AES/GCM 往返段。
- OWASP Cryptographic Storage Cheat Sheet（[cheatsheetseries.owasp.org](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)）：生成密钥 → `getEncoded()` 持久化 → `new SecretKeySpec(encoded, "AES")` 重建，强调用后清零。
- Jenkov《Java SecretKeySpec Tutorial》（[jenkov.com](https://jenkov.com/java-cryptography/secretkeyspec.html)）：`SecretKeySpec` 基本构造与 `getEncoded()` 用法示例。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/spec/SecretKeySpec.java`：`doSanityCheck`（null→"Missing argument"、空→"Empty key"，均 `IllegalArgumentException`，**不是 NPE**，源码与 JDK-27 实测一致）；`equals` 的 DESede/TripleDES 别名互认与 `MessageDigest.isEqual` 常数时间比较；4 参构造器的负偏移走 `ArrayIndexOutOfBoundsException`——黑盒异常段全部据此设计。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/spec/`（SecretKeySpec 相关用例）与 `test/jdk/com/sun/crypto/provider/Mac/HmacMD5.java` L40+：`new SecretKeySpec(keyBytes, "HmacMD5")` 喂 `Mac.init`——对应黑盒 Mac 驱动段。

## 4. 测试用例设计 + 自动化实现

`SecretKeySpecTest.java`，`testWhiteBox()`（结构，15 check）：public 具体类、extends Object、恰实现 `KeySpec`+`SecretKey` 两接口、2 个 public 构造器、方法名集合（含包内 `clear`）、`equals(Object)`/`byte[] getEncoded()` 精确签名。

`testBlackBox()`（行为，37 check）场景：

1. 基本访问器：getAlgorithm/getFormat=="RAW"/getEncoded 长度与内容；算法名原样返回（"aes" 传什么回什么）。
2. 防御拷贝三连：构造后改源数组不影响 key；两次 `getEncoded()` 不是同一引用但内容相同；改返回的拷贝不影响后续 `getEncoded()`。
3. 窗口构造器：取 `[16,32)` 恰 16 字节且首尾正确；对源数组的防御拷贝；`len=0` 合法（空材料 + 算法/format 正常）。
4. equals/hashCode：同字节同算法相等且 hashCode 相同；不同字节不等；同字节不同算法不等；算法名忽略大小写相等且 hashCode 相同；`DESede` vs `TripleDES` 别名相等；`equals(null)`/`equals("AES")` false；同源不同窗口（内容不同）不等、复制源同窗口相等。
5. 异常：2 参构造器 null key / null 算法 / 空 key → `IllegalArgumentException`（明确断言不是 NPE）；4 参构造器 null key / null 算法 / 空 key / 越界窗口 → `IllegalArgumentException`，负 offset / 负 len → `ArrayIndexOutOfBoundsException`。
6. 可用性组合：`SecretKeySpec(16B,"AES")` 驱动 `AES/GCM` 128-bit tag 加解密往返（密文=明文+16）；`SecretKeySpec(32B,"HmacSHA256")` 驱动 `Mac` 得 32 字节 tag。

## 5. JDK-8 版本差异
JDK-8 版本差异：无 clear() 成员（JDK 24+ 增加包私有 clear 销毁方法）；测试断言其不存在。代码使用本地 hex 帮助方法替代 JDK 17+ 的 java.util.HexFormat。
