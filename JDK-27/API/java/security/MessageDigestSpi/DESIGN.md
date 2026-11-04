# java.security.MessageDigestSpi

## 1. API 功能
消息摘要算法的服务提供者接口（SPI）。算法提供者通过继承该抽象类并实现 `engineUpdate(byte)`、`engineUpdate(byte[],int,int)`、`engineDigest()`、`engineReset()` 等引擎方法来提供具体的哈希实现。类本身提供了若干可继承的默认实现（如 `engineGetDigestLength()` 默认返回 0、`engineDigest(byte[],int,int)` 默认基于 `engineDigest()` 填充、`engineUpdate(ByteBuffer)` 默认逐块转发），以及 `clone()` 在 `instanceof Cloneable` 时浅拷贝。

## 2. Java 用户如何使用
用户通常不直接使用 SPI，而是通过 `MessageDigest.getInstance("SHA-256")` 获取封装后的实例。提供者则创建子类并（可选）实现 `Cloneable`，由 `MessageDigest` 引擎类调用这些 `engine*` 方法。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/MessageDigestSpi.java`（JDK 源）：
  - `public Object clone() { if (this instanceof Cloneable) { ... return o; } else { throw new CloneNotSupportedException(); } }`
  - `protected int engineDigest(byte[] buf, int offset, int len) { byte[] digest = engineDigest(); ... System.arraycopy(digest, 0, buf, offset, digest.length); return digest.length; }`
  - `protected int engineGetDigestLength() { return 0; }`
- `test/jdk/java/security/MessageDigest/TestCloneable.java`：
  - `MessageDigestSpi spi = (MessageDigestSpi) s.newInstance(null);`
  - `if (spi instanceof Cloneable) { ... MessageDigest md2 = (MessageDigest) md.clone(); ... }`

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：反射断言 `MessageDigestSpi` 为 public abstract class、超类为 `java.lang.Object`，并含 `clone`/`engineGetDigestLength`/`engineUpdate`/`engineDigest`/`engineReset` 方法。
- `testBlackBox()`：定义最小子类 `MyDigestSpi`（实现 `Cloneable`，`engineDigest()` 返回 `{9,9,9,9}`），断言默认 `engineGetDigestLength()==0`、`clone()` 返回非空且不同的实例、继承的 `engineDigest(byte[],int,int)` 把摘要写入指定数组并返回长度、`engineUpdate(ByteBuffer)` 使缓冲区 position 推进到 limit。
