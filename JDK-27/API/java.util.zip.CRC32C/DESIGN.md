# java.util.zip.CRC32C 测试方案

白盒结构断言在 `CRC32CTest.testWhiteBox()`，黑盒行为断言在 `CRC32CTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`CRC32C()`。
- **更新**：`update(int b)`、`update(byte[] b, int off, int len)`、`update(ByteBuffer buffer)`。
- **查询/重置**：`getValue()`、`reset()`。
- 语义：CRC-32C（Castagnoli）校验和，初始 0，`final` 类，实现 `Checksum`。

## 2. Java 用户如何使用

- 构造：`CRC32C c = new CRC32C();`。
- 典型调用：`c.update(bytes); long v = c.getValue();`；与 `CRC32` 用法一致但多项式不同（Castagnoli），常用于存储/网络校验。
- 真实组合链路：`new CRC32C()` → `update(...)` → `getValue()` → `reset()` 复用。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/TestCRC32C.java:37`：`ChecksumBase.testAll(new CRC32C(), 0xE3069283L);` —— 官方校验值 0xE3069283。
- `test/jdk/java/util/zip/TestChecksum.java:35`：`ChecksumBase.testAll(new MyCRC32C(), 0xE3069283L);` —— 通过 `Checksum` 接口默认方法复用同一校验值。
- `src/java.base/share/classes/java/util/zip/CRC32C.java`（类声明 `public final class CRC32C implements Checksum`）—— final 语义。

## 4. 测试用例设计 + 自动化实现

- **正常**：初始 0；`update(byte[],off,len)` 对 "123456789" 得到 `0xE3069283`；`update(int)`、`update(ByteBuffer)` 同值；`reset()` 回到 0。
- **组合**：增量更新等价于整块更新。
- **边界**：空输入仍为 0。
- **自动化**：`CRC32CTest.testBlackBox()` 实现；`testWhiteBox()` 校验 `final`、`implements Checksum`、方法存在。
- **健壮性**：固定输入、headless、无 `@key`。
