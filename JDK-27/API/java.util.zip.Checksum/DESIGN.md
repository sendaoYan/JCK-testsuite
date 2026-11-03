# java.util.zip.Checksum 测试方案

白盒结构断言在 `ChecksumTest.testWhiteBox()`，黑盒行为断言在 `ChecksumTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **抽象方法**：`update(int b)`、`update(byte[] b, int off, int len)`、`getValue()`、`reset()`。
- **默认方法**（JDK 9+）：`update(byte[] b)`（委托给 `update(b,0,b.length)`）、`update(ByteBuffer buffer)`（逐字节 `update(int)` 并推进 position 到 limit）。
- 说明：JDK-27 中不存在 `update(long)`（javap 已确认）。

## 2. Java 用户如何使用

- 自定义实现：`class Impl implements Checksum { ... }`，只需实现 4 个抽象方法即获得两个默认方法。
- 典型调用：`checksum.update(bytes); checksum.update(buf); long v = checksum.getValue(); checksum.reset();`。
- 真实组合链路：`update(byte[])`（默认）→ 内部 `update(byte[],off,len)` → 逐字节 `update(int)` → `getValue()`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/Checksum.java:57`：`default public void update(byte[] b)` —— 默认方法实现点。
- `src/java.base/share/classes/java/util/zip/Checksum.java:105`：`default public void update(ByteBuffer buffer)` —— 默认方法实现点。
- `test/jdk/java/util/zip/TestChecksum.java:35`：`ChecksumBase.testAll(new MyCRC32C(), 0xE3069283L);` —— 官方用自定义 `Checksum` 实现验证默认方法。
- `test/jdk/java/util/zip/ChecksumBase.java:54-98`：`reset()`/`update`/`getValue()` 组合测试。

## 4. 测试用例设计 + 自动化实现

- **正常**：匿名/静态实现只重写 4 个抽象方法；初始 `getValue()` 0；`update(int)` 与 `update(byte[],off,len)` 生效。
- **默认方法委托**：`update(byte[])` 恰好触发一次 `update(byte[],off,len)` 并逐字节 `update(int)`；`update(ByteBuffer)` 逐字节触发 `update(int)` 且 position 推进到 limit。
- **边界**：`reset()` 清零。
- **自动化**：`ChecksumTest.testBlackBox()` 用带调用计数器的 `Impl`；`testWhiteBox()` 用 `getMethod(...).isDefault()` 校验默认/抽象语义。
- **健壮性**：无墙钟/Random/无序集合，headless，无 `@key`。
