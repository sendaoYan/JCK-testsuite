# java.util.zip.Deflater 测试方案

白盒结构断言在 `DeflaterTest.testWhiteBox()`，黑盒行为断言在 `DeflaterTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **常量**：`DEFLATED`/`NO_COMPRESSION`/`BEST_SPEED`/`BEST_COMPRESSION`/`DEFAULT_COMPRESSION`/`FILTERED`/`HUFFMAN_ONLY`/`DEFAULT_STRATEGY`/`NO_FLUSH`/`SYNC_FLUSH`/`FULL_FLUSH`。
- **构造**：`Deflater()`、`Deflater(int level)`、`Deflater(int level, boolean nowrap)`。
- **输入/字典**：`setInput(byte[])`/`setInput(byte[],int,int)`/`setInput(ByteBuffer)`、`setDictionary(byte[])`/`setDictionary(byte[],int,int)`/`setDictionary(ByteBuffer)`。
- **压缩**：`deflate(byte[])`、`deflate(byte[],int,int)`、`deflate(byte[],int,int,int)`、`deflate(ByteBuffer)`、`deflate(ByteBuffer,int)`。
- **状态/查询**：`needsInput`、`finish`、`finished`、`setLevel`、`setStrategy`、`getAdler`、`getTotalIn/Out`、`getBytesRead/Written`、`reset`、`end`、`close`。

## 2. Java 用户如何使用

- 构造：`Deflater d = new Deflater();` 或指定 level/nowrap。
- 典型调用：`d.setInput(bytes); d.finish();` 循环 `deflate(out)` 直到 `finished()`；压缩完 `d.end()`。
- 真实组合链路：`setInput` → `finish` → 循环 `deflate` → `finished()` → `end()`；增量写入用 `setInput` 多次再 `finish`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/TotalInOut.java:43-44`：`Deflater deflater = new Deflater(); Inflater inflater = new Inflater();` —— 成对使用。
- `test/jdk/java/util/zip/TotalInOut.java:62-75`：`getBytesRead()`/`getBytesWritten()` 断言 —— 计数器语义。
- `test/jdk/java/util/zip/DeflaterDictionaryTests.java:94`：`deflater.setDictionary(DICTIONARY.getBytes(UTF_8), dictionary_offset, DICTIONARY_LENGTH);` —— 预设字典用法。
- `test/jdk/java/util/zip/DeflateIn_InflateOut.java:50-52`：`Deflater def = new Deflater(); ... new DeflaterInputStream(bais, def)` —— 与流类组合。

## 4. 测试用例设计 + 自动化实现

- **正常**：`needsInput` 初态 true；`setInput` 后 false；`finish` 后 `deflate` 产出且 `finished()`；`getBytesRead/getBytesWritten/getTotalIn/getTotalOut/getAdler` 计数正确。
- **组合**：deflate→inflate 往返还原内容；`setLevel(BEST_SPEED)+setStrategy(HUFFMAN_ONLY)` 后循环 `deflate(byte[],off,len)` 产出（Huffman 可能需多趟）；`setInput(ByteBuffer)`+`deflate(ByteBuffer)`；预设字典压缩→`needsDictionary`→`setDictionary`→还原。
- **边界**：`reset()` 清空计数器；`end()` 结束。
- **自动化**：`DeflaterTest.testBlackBox()`；`testWhiteBox()` 校验 `implements AutoCloseable`、常量值、构造器与方法存在。
- **健壮性**：固定输入、无墙钟/Random、headless、无 `@key`。
