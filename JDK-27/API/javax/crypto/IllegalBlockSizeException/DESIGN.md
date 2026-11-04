# javax.crypto.IllegalBlockSizeException — DESIGN（JDK-27 黑盒行为测试设计）

## 1. API 功能
- `public class IllegalBlockSizeException extends GeneralSecurityException`（`@since 1.4`）：提供给分组密码的数据长度不正确（非块大小整数倍等）时抛出。
- 构造器（javap JDK-27 确认，仅 2 个，**无 cause 构造器**）：`IllegalBlockSizeException()`、`IllegalBlockSizeException(String msg)`。
- 自身不声明任何方法；行为继承自 `Throwable`。固定 `serialVersionUID = -1965144811953540392L`。
- 继承链：→ `GeneralSecurityException` → `Exception`（受检）。

## 2. Java 用户如何使用
- 典型触发：NoPadding 模式加密/解密非整块输入（如 5 字节进 AES 16 字节块）；或带填充的分组密码解密非整块密文；或 `update()` 缓存的残块在 `doFinal()` 收尾时凑不成整块。
- 典型链路：`Cipher.getInstance("AES/CBC/NoPadding")` → `init(...)` → `doFinal(new byte[5])` → 抛本类；对照整块输入正常返回。
- 用户一般在 catch 中报告"输入长度必须是块大小整数倍"或切换到带填充的 transformation。

## 3. 开源代码实际用法调研
- JDK 源码（行为权威）`~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/IllegalBlockSizeException.java`：两构造器、无方法。抛出点：`com/sun/crypto/provider/CipherCore.doFinal`（"Input length not multiple of 16 bytes"）与 `NullCipher` 相关路径；`Cipher.doFinal` javadoc（`Cipher.java:2143` 起）`@throws IllegalBlockSizeException`。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/ByteBuffersNull.java:181`：自定义 CipherSpi `engineDoFinal` 声明 `throws IllegalBlockSizeException, BadPaddingException`（SPI 契约）。
- 开源（web 搜索结果，未逐字核对源码）：Apache TinkerPop/Directory、Confluent 等在 `Cipher.doFinal` 外同时 catch 本类与 `BadPaddingException`（[GitHub 代码搜索结果](https://github.com/search?q=%22IllegalBlockSizeException%22+language%3AJava&type=code)），常用于 NoPadding 场景长度校验报错。

## 4. 测试用例设计 + 自动化实现（IllegalBlockSizeExceptionTest.java）
白盒 `testWhiteBox()`（14 check）：public/非 abstract/非 final/直接父类 `java.security.GeneralSecurityException`/受检/Serializable/无自有方法/唯一字段 serialVersionUID(private static final long)/构造器集合恰 `{(), (String)}`/SUID 精确值。

黑盒 `testBlackBox()`（22 check）：
1. 构造行为：`()` → message/cause null（保留）；`("msg")` → 精确 message（保留）、toString 格式、getLocalizedMessage 同引用；构造捕获栈。
2. initCause 接线 + 二次 initCause → IllegalStateException。
3. 继承行为：instanceof GeneralSecurityException（保留 ×2）；upcast Exception 后 instanceof 本类仍真；throw 被父类/Exception handler 捕获同对象。
4. suppressed/setStackTrace；序列化往返。
5. **真实触发链路（核心，3 条 + 1 条对照）**：
   - `AES/CBC/NoPadding` 加密 5B（固定 key 0x00..0x0f、IV 0x20..0x2f）→ `doFinal(byte[])` 抛**精确 IllegalBlockSizeException**；
   - `update(5B)` + `doFinal()` 拆步调用 → 残块在 doFinal 报出，且经 `catch (GeneralSecurityException)` 捕获后仍断言精确类型；
   - `AES/ECB/PKCS5Padding` 解密 15B 非整块密文 → 本类；
   - 对照：整块 16B 输入加密成功、输出恰 16B。

确定性与隔离：固定 key/IV/输入字节；无随机/墙钟/文件/网络；异常 message 不断言。
