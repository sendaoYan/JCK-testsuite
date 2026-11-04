# javax.crypto.CryptoException — DESIGN（JDK-27 黑盒行为测试设计）

## 1. API 功能
- JDK-27 中为 `public final class CryptoException extends RuntimeException`，`@since 27`，`@PreviewFeature(feature = PEM_API)`（javap 与 `src.zip` 权威确认；**不是**早期 JDK 概念中的 checked crypto 基类）。javadoc：表示处理过程中的一般加密失败，用于不希望使用受检异常的 `GeneralSecurityException` 场景；provider 内部错误应改用 `java.security.ProviderException`。
- 构造器 4 个（本组唯一带 cause 的构造器集合）：
  - `CryptoException()` — message/cause 均 null。
  - `CryptoException(String message)`。
  - `CryptoException(String message, Throwable cause)` — cause 可为 null。
  - `CryptoException(Throwable cause)` — message 为 `(cause==null ? null : cause.toString())`。
- 固定 `serialVersionUID = -6824337376392797817L`；自身不声明方法，行为继承自 `Throwable`。

## 2. Java 用户如何使用
- 作为**非受检**加密失败在 PEM/EncryptedPrivateKeyInfo 新 API 中传播：调用方无需 throws 声明即可向上冒泡，`catch (CryptoException)` 精确处理或 `catch (RuntimeException)` 兜底。
- 典型链路（JDK-27 真实抛出方 `EncryptedPrivateKeyInfo.encrypt`，encryptImpl 内 `throw new CryptoException(e)`）：
  - `EncryptedPrivateKeyInfo.encrypt(BinaryEncodable, Key, algorithm, params, provider, random)` → 算法不受支持（`Cipher.getInstance` 抛 `NoSuchAlgorithmException`）或密钥非法（`Cipher.init` 抛 `InvalidKeyException`）→ 均被包装为本类，原异常经 `getCause()` 可得。
- 用户也可手动构造做异常转换（保留 cause 链）。

## 3. 开源代码实际用法调研
- JDK-27 源码（`src.zip`，行为权威，路径 `java.base/javax/crypto/CryptoException.java`）：final、extends RuntimeException、四构造器、PEM_API preview。
- JDK-27 抛出点（src.zip）：
  - `java.base/javax/crypto/EncryptedPrivateKeyInfo.java:500`（encryptImpl：`catch (NoSuchAlgorithmException | ... | InvalidKeyException e) { throw new CryptoException(e); }`）与 `:797`；javadoc `:366/:406/:449` 的 `@throws CryptoException`。
  - `java.base/java/security/PEMDecoder.java:244`（`catch (GeneralSecurityException e) { throw new CryptoException(e); }`，ENCRYPTED PRIVATE KEY 口令解密失败）与 `PEMEncoder.java:142`。
- JDK-27 自带测试：`~/git/jdk25u-dev` 为 JDK-25 树尚无本类（grep 证实），JDK-27 src 自带测试随 PEM API 演进；故开源第三方用法亦暂无沉淀——本设计以 JDK 源码抛出点为唯一权威依据（已逐字核对）。
- web 搜索：未检索到第三方开源项目引用本类（预期内：`@since 27` preview API）。

## 4. 测试用例设计 + 自动化实现（CryptoExceptionTest.java，保留 `@enablePreview`）
白盒 `testWhiteBox()`（15 check）：public/**final**/非 abstract/直接父类 `java.lang.RuntimeException`/非受检（assignable to RuntimeException 且**非** GeneralSecurityException）/Serializable/无自有方法/唯一字段 serialVersionUID(private static final long)/构造器集合恰 `{(), (String), (String,Throwable), (Throwable)}`/SUID 精确值。

黑盒 `testBlackBox()`（22 check）：
1. 构造矩阵（保留旧断言并扩展）：`()` message/cause null；`("msg")` 精确 message + toString 格式 + getLocalizedMessage 同引用；`("m",cause)` message 精确 + getCause 同引用；`(cause)` getCause 同引用且 message==cause.toString()；`("m",null)` cause 保持 null；构造捕获栈。
2. 非受检行为：辅助方法 `throwUnchecked()` 无 throws 子句抛出（编译期即断言 unchecked），运行时被 `catch (RuntimeException)` 捕获且 `getClass()==CryptoException.class`。
3. suppressed/setStackTrace；throw 被 Exception handler 捕获同对象；序列化往返保持类与 message。
4. **真实触发链路（核心，2 条）**：固定 16B AES key + 固定 PKCS8 编码字节：
   - `EncryptedPrivateKeyInfo.encrypt(spec, key, "NoSuchCipher/XYZ/NoPadding", ...)` → `CryptoException` 且 `getCause() instanceof NoSuchAlgorithmException`；
   - `EncryptedPrivateKeyInfo.encrypt(spec, 7B 坏 key, "AES/GCM/NoPadding", ...)` → `CryptoException` 且 `getCause() instanceof InvalidKeyException`。

确定性与隔离：固定 key/编码字节；preview API 用 jtreg `@enablePreview`（与同批次 BinaryEncodable 用法一致）；无随机/墙钟/文件/网络；异常 message 不断言。
