# javax.crypto.AEADBadTagException — DESIGN（JDK-27 黑盒行为测试设计）

## 1. API 功能
- `public class AEADBadTagException extends BadPaddingException`（`@since 1.7`），当 Cipher 运行于 AEAD 模式（如 GCM/CCM）且**认证标签验证失败**时抛出。
- 构造器（javap JDK-27 确认，仅 2 个，**无 cause 构造器**）：
  - `AEADBadTagException()` — 无详细消息。
  - `AEADBadTagException(String msg)` — 指定详细消息。
- 自身不声明任何方法；全部行为继承自 `Throwable`（getMessage/getCause/initCause/addSuppressed/setStackTrace/序列化）。固定 `serialVersionUID = -488059093241685509L`。
- 继承链：`AEADBadTagException` → `BadPaddingException` → `GeneralSecurityException` → `Exception`（受检异常）。

## 2. Java 用户如何使用
- 用户几乎从不手动构造，而是在解密处 `catch (AEADBadTagException e)` 区分"tag 验证失败（密文/AAD 被篡改或密钥错误）"与其他 `BadPaddingException`。
- 典型链路：`Cipher.getInstance("AES/GCM/NoPadding")` → `init(DECRYPT_MODE, key, GCMParameterSpec)` → `doFinal(密文)`：
  - 密文或 tag 任一字节被改 → 抛 `AEADBadTagException`（精确类型），可被 `catch (BadPaddingException)` 捕获。
- 也可手动构造用于异常转换（如把 JCE 异常包装成库自身异常前保留语义）。

## 3. 开源代码实际用法调研
- JDK 源码（行为权威）`~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/AEADBadTagException.java`：仅两个构造器、无方法；javadoc 写明"AEAD 模式无法验证 tag 时由 Cipher 抛出"。抛出实现位于 `com/sun/crypto/provider/GaloisCounterMode.java`（doFinal 校验 tag 失败处）与 `CipherCore.java`。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/Cipher/CipherInputStreamExceptions.java:82,216`：`catch (IOException e) { Throwable ec = e.getCause(); if (ec instanceof AEADBadTagException) ... }` — GCM 篡改密文经 CipherInputStream 读出时 cause 即本类。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/spec/GCMParameterSpec/GCMParameterSpecTest.java:59-65`：直接 `new AEADBadTagException(); new AEADBadTagException("Bad Tag Seen");` 冒烟构造器。
- 开源（web 搜索结果，未逐字核对源码）：[google/tink](https://github.com/tink-crypto/tink-java) 的 `AesGcmJce.decrypt` 在 `Cipher.getInstance("AES/GCM/NoPadding")` + `doFinal` 外捕获 `GeneralSecurityException`（AEADBadTagException 的祖先）并重包装；AWS DynamoDB Encryption Client、[apache/syncope](https://github.com/apache/syncope) 等在 `Cipher.doFinal` 周围捕获本类作为"密文被篡改/密钥错误"的信号并转换为自有异常。

## 4. 测试用例设计 + 自动化实现（AEADBadTagExceptionTest.java）
白盒 `testWhiteBox()`（14 check）：Class.forName 可达；public/非 abstract/非 final；直接父类二进制名 `javax.crypto.BadPaddingException`；受检（assignable to Exception 且非 RuntimeException）；Serializable；`getDeclaredMethods()==0`；唯一声明字段 `serialVersionUID` 为 private static final long；构造器集合恰为 `{(), (String)}` 且 public；`ObjectStreamClass.lookup(...).getSerialVersionUID()==-488059093241685509L`。

黑盒 `testBlackBox()`（23 check）：
1. 构造行为：`()` → getMessage/getCause 均 null（保留旧断言）；`("msg")` → 精确 message（保留）、`toString()=="javax.crypto.AEADBadTagException: msg"`、`getLocalizedMessage()` 与 getMessage 同引用；构造即捕获栈帧 ≥1。
2. cause 接线：`initCause(c)` 返回 this 且 getCause 同引用；二次 initCause → IllegalStateException。
3. 继承行为：instanceof BadPaddingException（保留）、GeneralSecurityException（保留）；向上转型 Exception 后 instanceof 本类仍真；throw 后被 `catch (BadPaddingException)` 与 `catch (Exception)` 捕到同一对象。
4. suppressed/stackTrace：addSuppressed→getSuppressed 同引用；setStackTrace→getStackTrace 回读。
5. 序列化往返：类与 message 保持。
6. **真实触发链路（核心）**：固定 key(0x00..0x0f)+12B IV(0x40..0x4b)+128-bit tag+固定 15B 明文，AES/GCM/NoPadding 加密得 pt+16B tag；未篡改密文解密往返一致（前置 sanity）；分别篡改密文首字节与 tag 末字节，解密必抛**精确 AEADBadTagException**（在 `catch (BadPaddingException)` 中断言 `getClass()==AEADBadTagException.class`）。

确定性与隔离：全部输入为固定字节，无随机/墙钟/文件/网络；异常 message 不断言（实现文案）。
