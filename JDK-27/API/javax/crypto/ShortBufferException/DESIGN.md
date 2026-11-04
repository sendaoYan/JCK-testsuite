# javax.crypto.ShortBufferException — DESIGN（JDK-27 黑盒行为测试设计）

## 1. API 功能
- `public class ShortBufferException extends GeneralSecurityException`（`@since 1.4`）：用户提供的**输出缓冲区太短**装不下操作结果时抛出。
- 构造器（javap JDK-27 确认，仅 2 个，**无 cause 构造器**）：`ShortBufferException()`、`ShortBufferException(String msg)`。
- 自身不声明任何方法；行为继承自 `Throwable`。固定 `serialVersionUID = 8427718640832943747L`。
- 继承链：→ `GeneralSecurityException` → `Exception`（受检）。

## 2. Java 用户如何使用
- 典型链路：`init` 后先 `getOutputSize(inputLen)` 预估输出长度；若直接给小缓冲调用 4 参 `update(input,0,len,out)` 或 5 参 `doFinal(input,0,len,out,outOff)` → 抛本类。
- `Cipher.doFinal` javadoc 明确规定恢复方式："a ShortBufferException is thrown. In this case, repeat this call with a larger output buffer"——即**换大缓冲重试同一调用**，本测试对该文档行为做端到端断言。
- 用户还常在 Mac.update/doFinal 与 CipherOutputStream 场景遇到它。

## 3. 开源代码实际用法调研
- JDK 源码（行为权威）`~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/ShortBufferException.java`；`Cipher.java:1952/1976-1981/2009/2072/2093`（4 参/5 参 update/doFinal 的 `@throws ShortBufferException` 与"repeat this call with a larger output buffer"文档）；实际抛点 `com/sun/crypto/provider/CipherCore`（"Output buffer must be (at least) N bytes long"）。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/ByteBuffersNull.java:169-179`：自定义 CipherSpi 的 `engineUpdate/engineDoFinal` 带 `throws ShortBufferException`，演示 SPI 契约位置。
- 开源（web 搜索结果，未逐字核对源码）：Apache Commons/Confluent 等在零拷贝加密路径用 `getOutputSize` 预分配并在 catch 本类后扩容重试（[GitHub 代码搜索结果](https://github.com/search?q=%22ShortBufferException%22+language%3AJava&type=code)）。

## 4. 测试用例设计 + 自动化实现（ShortBufferExceptionTest.java）
白盒 `testWhiteBox()`（14 check）：public/非 abstract/非 final/直接父类 `java.security.GeneralSecurityException`/受检/Serializable/无自有方法/唯一字段 serialVersionUID(private static final long)/构造器集合恰 `{(), (String)}`/SUID 精确值。

黑盒 `testBlackBox()`（24 check）：
1. 构造行为：`()` → message/cause null（保留）；`("msg")` → 精确 message（保留）、toString 格式、getLocalizedMessage 同引用；构造捕获栈。
2. initCause 接线 + 二次 initCause → IllegalStateException。
3. 继承行为：instanceof GeneralSecurityException（保留 ×2）；upcast Exception 后 instanceof 本类仍真；throw 被父类/Exception handler 捕获同对象。
4. suppressed/setStackTrace；序列化往返。
5. **真实触发链路（核心）**：固定 16B key + 16B IV，`AES/CBC/NoPadding`：
   - `getOutputSize(16)==16` 前置；
   - 5 参 `doFinal(new byte[16],0,16,new byte[8])` → **精确 ShortBufferException**；
   - 4 参 `update(new byte[16],0,16,new byte[4])` → 精确类型，经 `catch (GeneralSecurityException)` 捕获后再断言 `getClass()==ShortBufferException.class`；
   - 文档化恢复：重新 init 后用 `getOutputSize` 足量大缓冲（32B）重试成功，返回长度 16 且密文与直接 `doFinal(byte[])` 结果逐字节一致。

确定性与隔离：固定 key/IV/明文；无随机/墙钟/文件/网络；异常 message 不断言。
