# javax.crypto.NoSuchPaddingException — DESIGN（JDK-27 黑盒行为测试设计）

## 1. API 功能
- `public class NoSuchPaddingException extends GeneralSecurityException`（`@since 1.4`）：请求的填充机制在环境中不可用时由 `Cipher.getInstance` 抛出。
- 构造器（javap JDK-27 确认，仅 2 个，**无 cause 构造器**）：`NoSuchPaddingException()`、`NoSuchPaddingException(String msg)`。
- 自身不声明任何方法；行为继承自 `Throwable`。固定 `serialVersionUID = -4572885201200175466L`。
- 继承链：→ `GeneralSecurityException` → `Exception`（受检）。

## 2. Java 用户如何使用
- 典型链路：`Cipher.getInstance("AES/CBC/<padding>", provider)`。关键行为分界（JDK-27 `Cipher.java` 实测）：
  - **provider 显式形式**（`getInstance(String, String)` / `getInstance(String, Provider)`）：目标 provider 声明了算法服务但 `SupportedPaddings` 属性不含所请求填充 → 抛**NoSuchPaddingException**（"Padding not supported: ..."，Cipher.java:780-786 的 `paddingError` 分支）。
  - **无 provider 形式**（`getInstance(String)`）：全局搜索无任何 provider 支持该 transformation → 抛 `NoSuchAlgorithmException`（填充检查根本走不到）。
- SunJCE 对 "AES" 服务的 `SupportedPaddings = NOPADDING|PKCS5Padding|ISO10126Padding`，故任意其他填充名（如 `NoSuchPad123`）在 provider 形式下必抛本类——确定性触发。

## 3. 开源代码实际用法调研
- JDK 源码（行为权威）`~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/NoSuchPaddingException.java`；`~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/Cipher.java:779-786`：`paddingError != null → throw new NoSuchPaddingException("Padding not supported: " + paddingError)`；provider 侧抛点 `com/sun/crypto/provider/CipherCore.java:230/239`（"null padding"/"Padding: ... not available"）、`GaloisCounterMode.java:182`、`ChaCha20Cipher.java:178`（"Padding must be NoPadding"）。
- JDK 自带测试 `~/git/jdk8u-dev/test/jdk/javax/crypto/Cipher/TestGetInstance.java:98-108`：`Cipher.getInstance(algo + "/CBC/XYZPadding", "SunJCE")` 与 provider 对象两形式均 `catch (NoSuchPaddingException)`——本测试真实触发链路直接对齐该用例（"NoSuchPad123" 即 "XYZPadding" 角色）。
- 开源：使用面较窄，主要出现在允许用户自定义 transformation 字符串的加密库/配置解析中，把本类作为"填充名拼写错误"的输入校验信号（web 搜索结果，未逐字核对源码）。

## 4. 测试用例设计 + 自动化实现（NoSuchPaddingExceptionTest.java）
白盒 `testWhiteBox()`（14 check）：public/非 abstract/非 final/直接父类 `java.security.GeneralSecurityException`/受检/Serializable/无自有方法/唯一字段 serialVersionUID(private static final long)/构造器集合恰 `{(), (String)}`/SUID 精确值。

黑盒 `testBlackBox()`（24 check）：
1. 构造行为：`()` → message/cause null（保留）；`("msg")` → 精确 message（保留）、toString 格式、getLocalizedMessage 同引用；构造捕获栈。
2. initCause 接线 + 二次 initCause → IllegalStateException。
3. 继承行为：instanceof GeneralSecurityException（保留 ×2）；upcast Exception 后 instanceof 本类仍真；throw 被父类/Exception handler 捕获同对象。
4. suppressed/setStackTrace；序列化往返。
5. **真实触发链路（核心）**：
   - `Security.getProvider("SunJCE")` 非空前置断言（JDK 自带 provider）；
   - `Cipher.getInstance("AES/CBC/NoSuchPad123", "SunJCE")` → **精确 NoSuchPaddingException**；
   - `Cipher.getInstance("AES/CBC/NoSuchPad123", sunJCE对象)` → 同样精确类型，且经 `catch (GeneralSecurityException)` 捕获后再断言 `getClass()==NoSuchPaddingException.class`；
   - 对照 1：同形调用 `"AES/CBC/PKCS5Padding"` + "SunJCE" 正常解析且 provider 为 SunJCE；
   - 对照 2：无 provider 形式 `getInstance("AES/CBC/NoSuchPad123")` → `NoSuchAlgorithmException`（固化两异常分界）。

确定性与隔离：SunJCE 为 JDK 内置 provider（Temurin 必有）；无随机/墙钟/文件/网络；异常 message 不断言。
