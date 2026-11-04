# javax.imageio.stream.MemoryCacheImageInputStream — DESIGN

## 1. API 功能
`MemoryCacheImageInputStream` 是 `ImageInputStreamImpl` 的具体子类，将 `InputStream` 内容按需读入内存 `MemoryCache`，使顺序输入流获得随机访问能力。方法分组：
- 构造：`MemoryCacheImageInputStream(InputStream stream)`；`stream == null` 抛 `IllegalArgumentException`（精确类，消息 `"stream == null!"`）。
- 读：`read()`、`read(byte[],int,int)`；继承的 `readByte/readShort/readInt/readLong/readFloat/readDouble/readUTF/readFully/readBit(s)` 全部经由基类按 `byteOrder` 组装。
- 缓存状态矩阵：`isCached()`=true、`isCachedMemory()`=true、`isCachedFile()`=false。
- 位置/标记：`seek`、`mark`（栈式，可嵌套）、`reset`、`skipBytes`、`getStreamPosition`、`getBitOffset/setBitOffset`、`getFlushedPosition/flushBefore`、`close`。
- 该类不覆盖 `length()`，故恒返回 `-1L`（输入总长未知）。

## 2. Java 用户如何使用
典型链：`new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))` → 确认 `isCached()/isCachedMemory()` → 顺序/随机读 → `mark()+readFully()+reset()` 回退 → `setByteOrder(LITTLE_ENDIAN)` 双字节序 round-trip → `flushBefore(pos)` 释放前缀缓存 → `close()`。`ImageIO.createImageInputStream(InputStream)` 在未启用文件缓存时（`ImageIO.setUseCache(false)` 默认）返回本类。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/MemoryCacheImageInputStream.java`：`read()` 先 `cache.loadFromStream(stream, streamPos+1)` 再 `cache.read(streamPos++)`，即读到哪缓存到哪。
- `com/sun/imageio/spi/InputStreamImageInputStreamSpi.java`：`ImageIO.createImageInputStream(InputStream)` 的生产者。
- `jdk/test/javax/imageio/stream/ReadFullyTest.java`、`ReadBytesIIOByteBuffer.java`：以本类为 `ImageInputStream` 实现做 readFully/read(byte[]) 契约验证。

## 4. 测试用例设计 + 易错规范细节（`MemoryCacheImageInputStreamTest` testDeep*）
- 构造与缓存状态矩阵：null → 精确 `IllegalArgumentException`；`isCached/isCachedMemory/!isCachedFile`；默认 `BIG_ENDIAN`；初始 `streamPos=0`、`bitOffset=0`、`flushedPos=0`。
- 基本类型读：BE/LE 双字节序下 `readByte/readUnsignedByte/readShort/readUnsignedShort/readChar/readInt/readLong/readFloat/readDouble/readUTF` 精确值断言。
- EOF 精度：剩 1 字节时 `readShort` 抛精确 `EOFException`（部分填充后抛出）；`readLong` 剩 3 字节同理；`read()` EOF 返回 `-1`；`readBit/readBits` 在 EOF 抛 `EOFException`。
- 边界负例（全部 `t.getClass()==X.class` 精确断言）：`read(null,0,1)` → `NullPointerException`；`readFully(b,-1,1)`/越界 → `IndexOutOfBoundsException`；`seek(-1)`/`seek(<flushedPos)` → `IndexOutOfBoundsException`；`setBitOffset(-1)`/`(8)` → `IllegalArgumentException`；`flushBefore(-1)`/`flushBefore(>pos)` → `IndexOutOfBoundsException`；`skipBytes(-1)` → `IndexOutOfBoundsException`。
- mark/reset 深度：嵌套两层 `mark`，逐层 `reset` 回到各自位置；`reset` 无 mark → 精确 `IOException`；`reset` 回到已 flush 区域 → 精确 `javax.imageio.IIOException`（"Position to reset to is less than the flushed position"，注意是 IIOException 不是普通 IOException）。
- flushBefore：释放后 `getFlushedPosition` 精确推进；`seek` 不得低于 flushedPos。
- skipBytes：返回实际跳过数；越过 EOF 返回 n（实现为纯 seek）；负数参数抛 IOOBE。
- 位操作：`readBit` MSB 优先；`readBits(n)` 跨字节、`bitOffset` 回卷；读后位置/位偏移精确断言。
- close 状态：第二次 `close()` → 精确 `IOException`("closed")；close 后 `read/seek/getStreamPosition/length/flushBefore` 全部精确 `IOException`。
- 版本差异：JDK-8 与 JDK-27 公开 API（javap 对照）完全一致，本类行为亦一致。

## 5. 未写入 Javadoc 的实测行为
- `length()` 恒 `-1`（未在类 Javadoc 中说明，仅在 `ImageInputStream.length()` 泛化描述）。
- `skipBytes(n)` 越过 EOF 返回 n 而非短跳数（基类实现为 `seek(streamPos+n)`）。
- `setByteOrder(null)` 被接受，之后按 LITTLE_ENDIAN 解码（基类 `readShort` 等的 null 分支）。
- `reset` 落入已 flush 区域抛 `javax.imageio.IIOException`，是普通 `IOException` 的子类 —— 用精确类断言才能与"无 mark 的 reset"区分。
