# javax.crypto.BadPaddingException — DESIGN（JDK-27 黑盒行为测试设计）

## 1. API 功能
- `public class BadPaddingException extends GeneralSecurityException`（`@since 1.4`）：期望某种填充机制而数据填充不正确时由 Cipher 抛出。
- 构造器（javap JDK-27 确认，仅 2 个，**无 cause 构造器**）：`BadPaddingException()`、`BadPaddingException(String msg)`。
- 自身不声明任何方法；行为全部继承自 `Throwable`。固定 `serialVersionUID = -5315033893984728443L`。
- 继承链：→ `GeneralSecurityException` → `Exception`（受检）；子类 `AEADBadTagException`。

## 2. Java 用户如何使用
- 在解密处 `catch (BadPaddingException)`：表示"填充不合法"（密钥错误/密文损坏/填充被篡改）。注意它**不会**在输入长度非整块时报出（那是 `IllegalBlockSizeException`），两类异常的分界是核心行为。
- 典型链路：`Cipher.getInstance("AES/CBC/PKCS5Padding")` → `init(DECRYPT_MODE, key, iv)` → `doFinal(ct)`；PKCS#5 unpad 发现末字节不在 1..16 或填充字节不一致 → 抛本类。
- 手动构造用于包装/转发（如 JDK `CipherOutputStream.close`、SealedObject 解封路径都把它向外传播）。

## 3. 开源代码实际用法调研
- JDK 源码（行为权威）`~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/BadPaddingException.java`：两构造器、无方法。实际抛出点：`com/sun/crypto/provider/PKCS5Padding.java`（unpad 校验失败）、`CipherCore.doFinal`；`Cipher.doFinal` 的 javadoc（`~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/Cipher.java:2148` 起）声明 `@throws BadPaddingException`。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/CipherInputStreamExceptions.java`：坏填充密文经流式读取抛出后以 `BadPaddingException` 分支校验。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/ByteBuffersNull.java:175-203`：自定义 CipherSpi 的 `engineDoFinal` 签名声明 `throws IllegalBlockSizeException, BadPaddingException`，演示 SPI 契约位置。
- 开源（web 搜索结果，未逐字核对源码）：Apache Commons/Directory、Confluent 等项目在 `Cipher.doFinal` 解密周围 `catch (BadPaddingException)` 并转换为口令错误类异常（[GitHub 代码搜索结果](https://github.com/search?q=%22BadPaddingException%22+language%3AJava&type=code)）。

## 4. 测试用例设计 + 自动化实现（BadPaddingExceptionTest.java）
白盒 `testWhiteBox()`（14 check）：public/非 abstract/**非 final**（AEADBadTagException 需继承它）/直接父类 `java.security.GeneralSecurityException`/受检/Serializable/无自有方法/唯一字段 serialVersionUID(private static final long)/构造器集合恰 `{(), (String)}`/SUID 精确值。

黑盒 `testBlackBox()`（23 check）：
1. 构造行为：`()` → message/cause null（保留旧断言）；`("msg")` → 精确 message（保留）、toString 格式、getLocalizedMessage 同引用；构造捕获栈。
2. initCause 接线 + 二次 initCause → IllegalStateException。
3. 继承行为：instanceof GeneralSecurityException（保留 ×2 语义）；upcast Exception 后 instanceof 本类仍真；throw 被 `catch (GeneralSecurityException)`、`catch (Exception)` 捕获同对象。
4. suppressed/setStackTrace 回读；序列化往返保持类与 message。
5. **真实触发链路（核心，确定性构造非法填充）**：固定 16B AES key，`AES/ECB/PKCS5Padding` 加密 16B 全零明文 → 恰好 2 块密文 C1‖C2（PKCS#5 对整块明文补整块 0x10×16）。伪造密文 C1‖C1：ECB 解密得 全零‖全零，末字节 0x00 不在合法填充长度 1..16 → **必抛精确 BadPaddingException**（key/pt 固定故确定性，无概率）。前置 sanity：原密文解密 unpad 回 16B 全零。
6. 边界对照：同一 cipher 解密 15B（非整块）→ `IllegalBlockSizeException` 而非本类，固化两类异常分界。

确定性与隔离：固定 key/明文/伪造密文；无随机/墙钟/文件/网络；异常 message 不断言。
