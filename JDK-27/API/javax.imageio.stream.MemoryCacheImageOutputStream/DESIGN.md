# javax.imageio.stream.MemoryCacheImageOutputStream — DESIGN

## 1. API 功能
`MemoryCacheImageOutputStream` 是 `ImageOutputStreamImpl` 的具体子类，写入先进入内存 `MemoryCache`，在 `flushBefore(pos)` 或 `close()` 时把前缀字节刷入底层 `OutputStream`。方法分组：
- 构造：`MemoryCacheImageOutputStream(OutputStream stream)`；`null` 抛 `IllegalArgumentException`。
- 写：`write(int)`、`write(byte[],int,int)`（继承的 `writeByte/writeInt/writeUTF/...`）。
- 读/回退/长度：`read()`、`read(byte[],int,int)`、`seek(long)`、`length()`（缓存长度）。
- 缓存状态：`isCached()`（true）、`isCachedMemory()`（true）、`isCachedFile()`（false）。
- 丢弃：`flushBefore(long)`；`close()` 刷全部并释放缓存。

## 2. Java 用户如何使用
典型链：`new MemoryCacheImageOutputStream(new ByteArrayOutputStream())` → `writeByte/writeInt/writeBoolean/writeUTF/writeShort` 写 → `length()/getStreamPosition()` → `seek(0)` + `readByte/readInt/...` 读回 → `flushBefore(pos)` + `getFlushedPosition()` → `close()` 后 `ByteArrayOutputStream.toByteArray()` 取最终字节。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/MemoryCacheImageOutputStream.java`：`flushBefore` 把 `flushBytes` `cache.writeToStream(stream, ...)` 后 `cache.disposeBefore` 并 `stream.flush()`；`close()` 先 `seek(length)` 再 `flushBefore(length)`。
- `src/java.desktop/share/classes/com/sun/imageio/spi/OutputStreamImageOutputStreamSpi.java`：`ImageIO.createImageOutputStream(OutputStream)` 在 `useCache == false` 时返回 `MemoryCacheImageOutputStream`。
- `test/jdk/javax/imageio/stream/MemoryCacheImageOutputStreamTest.java`：大数组写 + 散点 `seek` 写 + `flushBefore` 后读回校验。

## 4. 测试用例设计 + 自动化实现（`MemoryCacheImageOutputStreamTest.testBlackBox`）
- 固定 `ByteArrayOutputStream` 构造，断言 `isCached/isCachedMemory/!isCachedFile`。
- 写 `writeByte/writeInt/writeBoolean/writeUTF("hi")/writeShort`，断言 `length()==12`、`getStreamPosition()==12`。
- `seek(0)` 读回 `readByte/readInt/readBoolean/readUTF/readShort`。
- `seek(5)` + `flushBefore(5)`，断言 `getFlushedPosition()==5`、底层流已收到 5 字节且内容正确。
- `close()` 后断言底层流共 12 字节。
