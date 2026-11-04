# java.util.zip.Inflater 测试方案

白盒结构断言在 `InflaterTest.testWhiteBox()`，黑盒行为断言在 `InflaterTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`Inflater()`、`Inflater(boolean nowrap)`。
- **输入/字典**：`setInput(byte[])`、`setInput(byte[],int,int)`、`setInput(ByteBuffer)`、`setDictionary(byte[])` 等。
- **解压**：`inflate(byte[])`、`inflate(byte[],int,int)`、`inflate(ByteBuffer)`（抛 `DataFormatException`）。
- **状态/查询**：`needsInput`、`needsDictionary`、`finished`、`getRemaining`、`getAdler`、`getTotalIn/Out`、`getBytesRead/Written`、`reset`、`end`、`close`。

## 2. Java 用户如何使用

- 构造：`Inflater inf = new Inflater();`（或 `nowrap=true` 解压裸 deflate 流）。
- 典型调用：`inf.setInput(compressed);` 循环 `inflate(out)` 直到 `finished()`；`inf.end()`。
- 真实组合链路：`setInput` → `inflate` → `finished()`；字典场景：`inflate` 后 `needsDictionary()` → `setDictionary` → 继续 `inflate`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/TotalInOut.java:43-44`：`Deflater/Inflater` 成对构造。
- `test/jdk/java/util/zip/TotalInOut.java:73-75`：`getBytesWritten()/getBytesRead()` 断言。
- `test/jdk/java/util/zip/DeflaterDictionaryTests.java:107`：`if (inflater.needsDictionary())` —— 字典解压判断。

## 4. 测试用例设计 + 自动化实现

- **正常**：`needsInput` 初态 true、`needsDictionary` false；deflate 压缩后 inflate 还原长度与内容；`finished()` true；`getRemaining` 0；`getAdler/getBytesRead/getBytesWritten/getTotalIn/getTotalOut` 正确。
- **组合**：`reset()` 回到初态；`nowrap=true` 与 `Deflater(nowrap=true)` 往返；`inflate(byte[],off,len)` 还原。
- **自动化**：`InflaterTest.testBlackBox()`；`testWhiteBox()` 校验 `implements AutoCloseable`、构造器与方法存在。
- **健壮性**：固定输入、无墙钟/Random、headless、无 `@key`。
