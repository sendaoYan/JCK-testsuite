# javax.imageio.stream.ImageInputStream — DESIGN

## 1. API 功能
`ImageInputStream` 是 `java.io.DataInput` + `java.io.Closeable` 的子接口，定义可随机读（seek/mark/reset/skip）的二进制输入流。方法分组：
- 字节序：`setByteOrder(ByteOrder)`、`getByteOrder()`（默认 `BIG_ENDIAN`）。
- 原始读：`read()`、`read(byte[])`、`read(byte[],int,int)`、`readBytes(IIOByteBuffer,int)`、`readBoolean/readByte/readUnsignedByte/readShort/readUnsignedShort/readChar/readInt/readUnsignedInt/readLong/readFloat/readDouble/readLine/readUTF`。
- 批量读：8 个 `readFully` 重载（byte/short/char/int/long/float/double 数组）。
- 位置/位：`getStreamPosition()`、`getBitOffset()`、`setBitOffset(int)`、`readBit()`、`readBits(int)`、`length()`、`skipBytes(int/long)`、`seek(long)`。
- 标记/丢弃：`mark()`、`reset()`、`flushBefore(long)`、`flush()`、`getFlushedPosition()`、`isCached/isCachedMemory/isCachedFile()`、`close()`。

## 2. Java 用户如何使用
用户不直接 new，而是 `ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))`。典型组合链：`setByteOrder(LITTLE_ENDIAN)` → `readByte/readShort/readInt/readLong/readFloat/readDouble/readBoolean/readChar/readUTF` 顺序解码 → `readBytes(buf,2)` → `seek(0)`/`mark()+reset()` 回退 → `read(byte[],off,len)`/`readFully(byte[])` → `skipBytes(n)` → `flushBefore(pos)` + `getFlushedPosition()` → `close()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/ImageInputStreamImpl.java`：接口的默认实现（`readShort/readInt/readUTF/readFully/mark/reset/seek/flushBefore` 等）。
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：大量 `readInt/readUnsignedShort/readUTF` 组合解析图像头部。
- `test/jdk/javax/imageio/stream/ReadUnsignedIntTest.java`：用 `ImageIO.createImageInputStream` + `DataOutputStream` 编码后 `readUnsignedInt`。
- `test/jdk/javax/imageio/stream/ReadFullyTest.java`：`MemoryCacheImageInputStream` 上验证 `readFully` 大/小端。

## 4. 测试用例设计 + 自动化实现（`ImageInputStreamTest.testBlackBox`）
- 用 `DataOutputStream` 编码固定序列（byte/short/int/long/float/double/boolean/char/UTF/bytes/chars），经 `ImageIO.createImageInputStream` 读取并逐个断言相等。
- 断言默认 `BIG_ENDIAN`、`setByteOrder` 往返、`setBitOffset` 往返、`isCached()`。
- `mark/reset`、`read(byte[],off,len)`、`readFully(byte[])` 全程往返、`skipBytes(int)`、`length()==-1`。
- `flushBefore` + `flush` + `getFlushedPosition` 及 `seek` 越界抛 `IndexOutOfBoundsException`。
