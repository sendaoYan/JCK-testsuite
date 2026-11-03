# javax.crypto.DecapsulateException 设计案

## 1. API 功能
JDK 21+ 的受检异常，`public class DecapsulateException extends GeneralSecurityException`，恰好两个公共构造器（JDK 27 javap：**无**无参构造器）：
- `DecapsulateException(String message)`
- `DecapsulateException(String message, Throwable cause)`
由 `KEM.Decapsulator.decapsulate(...)` 与 `KEMSpi.DecapsulatorSpi.engineDecapsulate(...)` 声明抛出，表示解封装失败。

## 2. Java 用户如何使用
调用方 `try { SecretKey k = dec.decapsulate(ct); ... } catch (DecapsulateException e) { /* 密文损坏/长度错/provider 校验失败 */ }`；因是 GeneralSecurityException 子类，也可并入上层 `catch (GeneralSecurityException)` 统一处理。自定义 KEMSpi 实现中 `throw new DecapsulateException("reason", cause)` 携带根因。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/DecapsulateException.java`：仅两个构造器，extends GeneralSecurityException。
- `src/java.base/share/classes/javax/crypto/KEM.java` L348/L365 javadoc：`@throws DecapsulateException if an error occurs during the decapsulation process`（尺寸不符即抛）。
- `src/java.base/share/classes/com/sun/crypto/provider/DHKEM.java`：真实抛点——全零/非法密文 "Cannot decapsulate"、长度不符 "incorrect encapsulation size"（本机 JDK 27.0.1 实测）。
- `test/jdk/javax/crypto/KEM/KemTest.java` L146-148：`try { decT.decapsulate(new byte[enc.encapsulation().length]); Asserts.fail(...); } catch (DecapsulateException e) { /* expected */ }`。
- 实测补充：`(String, null)` 构造器会消耗 cause 槽位——之后 `initCause` 抛 IllegalStateException（Throwable.initCause javadoc："can be called at most once"）。

## 4. 测试用例设计 + 自动化实现
`DecapsulateExceptionTest.java` 白盒（5 check）：public、非抽象非 final、父类二进制名 java.security.GeneralSecurityException、恰好 2 个公共构造器且形态为 (String)/(String,Throwable)。黑盒（14 check）场景：
1. 保留旧断言：message 构造器取值、instanceof GeneralSecurityException、cause 构造器身份。
2. message 形态：cause null、suppressed 空、toString 为 "javax.crypto.DecapsulateException: msg"。
3. message+cause 形态：message/cause/cause.getMessage 精确，toString 含两者。
4. (String, null)：getCause null 且 initCause 抛 IllegalStateException（cause 槽已被构造器占用）。
5. 以 `catch (GeneralSecurityException)` 多态捕获成功。
6. 手写 KEMSpi.DecapsulatorSpi 抛出后 message 与 IllegalStateException 根因原样传播。
7. 真实 provider 失败：全零 32B X25519 密文抛出且 instanceof GSE、message 非空；同解封器对合法密文仍成功（对照）。
