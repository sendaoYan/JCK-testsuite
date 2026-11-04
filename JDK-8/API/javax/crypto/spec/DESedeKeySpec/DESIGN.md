# javax.crypto.spec.DESedeKeySpec 设计案

## 1. API 功能
三重 DES（DES-EDE）密钥材料规格，`public class implements java.security.spec.KeySpec`，无密码学算法逻辑的纯数据类：
- 构造：`DESedeKeySpec(byte[] key)`（取前 24 字节）与 `DESedeKeySpec(byte[] key, int offset)`（取 [offset, offset+24)）；两构造器均做防御拷贝；材料不足 24 字节抛 `InvalidKeyException("Wrong key size")`。
- 查询：`getKey()` 返回 24 字节密钥副本（每次新数组）。
- 静态校验：`isParityAdjusted(byte[], int)` 检查 3 个连续 8 字节 DES 子键是否均为奇校验（委托 `DESKeySpec.isParityAdjusted` 三次，offset/offset+8/offset+16），材料不足抛 `InvalidKeyException`。
- 常量：`DES_EDE_KEY_LEN = 24`。

## 2. Java 用户如何使用
构造 `new DESedeKeySpec(key24)` 或带 offset 从更长缓冲取窗口 → `getKey()` 读回 → `isParityAdjusted(key, 0)` 预检 → 真实链路：`SecretKeyFactory.getInstance("DESede").generateSecret(spec)` 得 DESede SecretKey → `Cipher.getInstance("DESede/CBC/PKCS5Padding")` 加解密。构造器不做校验位校正（SunJCE `DESedeKey` 在 generateSecret 时校正，奇校验输入则 getEncoded 与输入逐字节相等）。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/DESedeKeySpec.java`：构造器长度检查 `key.length - offset < 24 → InvalidKeyException`；`getKey()` 返回 `this.key.clone()`；`isParityAdjusted` 委托 DESKeySpec 检查 3 个子键。
- `test/jdk/com/sun/crypto/provider/Cipher/DES/DesAPITest.java` L100-109：`new DESedeKeySpec(key)` → `SecretKeyFactory.getInstance("DESede","SunJCE")` → `factory.generateSecret(desKeySpec)` → Cipher 加解密（本测试真实链路同款）。
- `src/java.base/com/sun/crypto/provider/DESedeKey.java`：构造时 `DESKeyGenerator.setParityBit` 校正校验位——实测奇校验输入下 getEncoded 与 spec 相等。

## 4. 测试用例设计 + 自动化实现
`DESedeKeySpecTest.java` 白盒（9 check）：public/非抽象/extends Object/implements KeySpec、getKey/isParityAdjusted 方法名、两公共构造器形态、DES_EDE_KEY_LEN==24 且 public static final。黑盒（24 check）场景：
1. 保留旧断言：常量 24、`new byte[24]` 构造 + getKey 长度、`0x01` 填充 isParityAdjusted true。
2. 固定 24 字节奇校验 key（`oddParity()` 工具逐字节调整）：getKey 内容精确、防御拷贝（改返回数组/改源数组均不影响、两次 getKey 不同引用）。
3. offset 构造器窗口精确（40 字节缓冲 offset 16）。
4. isParityAdjusted true/0x01 填充/偶校验字节(0x03)/全零 false/offset 窗口两例。
5. instanceof KeySpec。
6. 异常：23 字节 ctor、offset 越界 ctor、isParityAdjusted 短材料 → InvalidKeyException。
7. 真实链路：奇校验 spec → SecretKeyFactory("DESede").generateSecret → getEncoded 与 key 逐字节相等 + 算法名 DESede → Cipher DESede/CBC/PKCS5Padding 27 字节明文 → 32 字节密文 → 解密往返相等；offset spec 与普通 spec 工厂产物一致。
