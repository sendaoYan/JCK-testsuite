# java.util.zip.Adler32 测试方案

白盒结构断言在 `Adler32Test.testWhiteBox()`，黑盒行为断言在 `Adler32Test.testBlackBox()`。本文件聚焦黑盒方案。

## 1. API 功能（按方法分组）

- **构造**：`Adler32()`。
- **更新**：`update(int b)`、`update(byte[] b, int off, int len)`、`update(ByteBuffer buffer)`。
- **查询/重置**：`getValue()`、`reset()`。
- 语义：Adler-32 校验和，初始值 1，可增量累积，`reset()` 回到初始 1，`getValue()` 返回无符号 32 位值（`long`）。

## 2. Java 用户如何使用

- 构造：`Adler32 a = new Adler32();`（初始值即 1，无需额外初始化）。
- 典型调用：`a.update(data, 0, data.length); long v = a.getValue();`；分块增量：`update(前半)` + `update(后半)` 等价于一次 `update(整块)`。
- 真实组合链路：`new Adler32()` → `update(byte[])`/`update(ByteBuffer)` → `getValue()` →（需要时）`reset()` 再复用同一实例。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `src/java.base/share/classes/java/util/zip/Adler32.java:46`：`public class Adler32 implements Checksum` —— 类声明及 `Checksum` 契约。
- `src/java.base/share/classes/sun/security/ssl/SSLSessionImpl.java:613`：`Adler32 adler32 = new Adler32();` —— JDK 内部真实自用（SSL 会话 ID 计算）。
- `test/jdk/java/util/zip/ChecksumBase.java:63`：`checksum.update(BYTES_123456789);` —— 官方测试用 "123456789" 校验值。
- `test/jdk/java/util/zip/ChecksumBase.java:98`：`checksum.getValue() != expected` —— 官方测试对 `getValue()` 精确断言。

## 4. 测试用例设计 + 自动化实现

- **正常**：初始值 1；`update(byte[],off,len)` 对 "123456789" 得到 `0x091E01DE`；`update(int)` 逐字节与 `update(ByteBuffer)` 得到相同值；"Wikipedia" 得到 `0x11E60398`。
- **组合**：增量更新（先 4 字节后 5 字节）等价于整块更新；`reset()` 后回到 1。
- **边界**：空输入 `update(new byte[0],0,0)` 后仍为 1。
- **自动化**：`Adler32Test.testBlackBox()` 内直接实现，`testWhiteBox()` 用 `Class.forName` + `Checksum.isAssignableFrom` + `getMethod` 校验结构。
- **健壮性**：固定字节输入、无墙钟/Random/无序集合断言，纯 headless，无 `@key`。
