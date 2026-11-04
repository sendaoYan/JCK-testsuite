# javax.crypto.spec.DESKeySpec 设计案

## 1. API 功能
DES 密钥材料规格，`public class implements java.security.spec.KeySpec`：
- 构造：`DESKeySpec(byte[] key)`（前 8 字节）/ `DESKeySpec(byte[] key, int offset)`（[offset, offset+8)）；防御拷贝；不足 8 字节抛 `InvalidKeyException("Wrong key size")`。
- 查询：`getKey()` 返回 8 字节副本。
- 静态校验：`isParityAdjusted(byte[], int)`——每字节 bit 数为奇则 true；`isWeak(byte[], int)`——对照 FIPS 74 的 16 个弱/半弱 key 表（含 0x0101010101010101、0xFEFEFEFEFEFEFEFE 等）；材料不足均抛 `InvalidKeyException`。
- 常量：`DES_KEY_LEN = 8`。

## 2. Java 用户如何使用
`new DESKeySpec(key8)` → `isParityAdjusted`/`isWeak` 预检弱密钥 → `SecretKeyFactory.getInstance("DES").generateSecret(spec)`（SunJCE 的 DESKey 会 `setParityBit` 校正校验位，奇校验输入则 getEncoded 与输入相等）→ `Cipher.getInstance("DES/CBC/PKCS5Padding")` 加解密。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/DESKeySpec.java`：WEAK_KEYS 表（FIPS 74 全部 16 个半弱/弱 key 十六进制硬编码）；`isParityAdjusted` 逐字节检查 bit 奇偶；构造/getKey 与 DESedeKeySpec 同款防御拷贝模式。
- `test/jdk/com/sun/crypto/provider/Cipher/DES/DesAPITest.java` L103-109：`new DESKeySpec(key)` → `SecretKeyFactory.getInstance("DES","SunJCE").generateSecret(desKeySpec)` → Cipher 加解密（本测试真实链路同款）。
- `src/java.base/com/sun/crypto/provider/DESKey.java`：构造器调用 `DESKeyGenerator.setParityBit(this.key, 0)` 校正校验位——实测奇校验输入下工厂 getEncoded 与 spec 相等。

## 4. 测试用例设计 + 自动化实现
`DESKeySpecTest.java` 白盒（8 check）：public/非抽象/extends Object/implements KeySpec、getKey/isParityAdjusted/isWeak 方法名、两构造器形态、DES_KEY_LEN==8 且 public static final。黑盒（25 check）场景：
1. 保留旧断言：DES_KEY_LEN==8、`new byte[8]` 构造 + getKey 长度 8、`{0x01}^8` isParityAdjusted true、全零 false、isWeak(全零) false。
2. 固定奇校验 key8（`oddParity()` 工具）：getKey 内容精确、防御拷贝（返回数组/源数组）、offset 构造器窗口、全零 key 可构造。
3. isParityAdjusted：0x03（2 bit）false、offset 窗口 true。
4. isWeak：0x01×8 true、普通 key false、16 个 FIPS-74 弱 key 全部检出（计数 ==16）、offset 窗口检出。
5. instanceof KeySpec。
6. 异常：7 字节 ctor、offset 越界、isWeak/isParityAdjusted 短材料 → InvalidKeyException。
7. 真实链路：奇校验 spec → SecretKeyFactory("DES") → getEncoded 精确相等 + 算法名 DES → Cipher DES/CBC/PKCS5Padding 加解密往返；offset spec 工厂产物一致。
