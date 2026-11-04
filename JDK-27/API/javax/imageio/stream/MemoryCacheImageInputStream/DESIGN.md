# javax.imageio.stream.MemoryCacheImageInputStream — DESIGN

## 1. API 功能
`MemoryCacheImageInputStream` 是 `ImageInputStreamImpl` 的具体子类，把 `InputStream` 的内容按需读入内存 `MemoryCache`，从而在顺序输入流上提供随机访问。方法分组：
- 构造：`MemoryCacheImageInputStream(InputStream stream)`；`null` 抛 `IllegalArgumentException`。
- 读：`read()`、`read(byte[],int,int)`（继承的 `readByte/readInt/readFully/...` 也基于此工作）。
- 缓存状态：`isCached()`（true）、`isCachedMemory()`（true）、`isCachedFile()`（false）。
- 丢弃：`flushBefore(long)`（释放前缀缓存）；`close()`。
- 注意：该类不覆盖 `length()`，故 `length()` 恒为 `-1L`。

## 2. Java 用户如何使用
典型链：`new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))` → `isCached()/isCachedMemory()` 确认内存缓存 → `read()`/`readByte()` 顺序读 → `seek(0)`/`mark()+readFully()+reset()` 回退 → `skipBytes(n)` → `flushBefore(pos)` + `getFlushedPosition()` → `close()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/MemoryCacheImageInputStream.java`：`read()` 用 `cache.loadFromStream(stream, streamPos+1)` 后 `cache.read(streamPos++)`。
- `src/java.desktop/share/classes/com/sun/imageio/spi/InputStreamImageInputStreamSpi.java`：`ImageIO.createImageInputStream(InputStream)` 在 `useCache == false` 时返回 `MemoryCacheImageInputStream`。
- `test/jdk/javax/imageio/stream/ReadFullyTest.java`、`ReadBytesIIOByteBuffer.java`：均用 `MemoryCacheImageInputStream` 作为 `ImageInputStream` 实现。

## 4. 测试用例设计 + 自动化实现（`MemoryCacheImageInputStreamTest.testBlackBox`）
- 固定 24 字节（1..24）构造，断言 `isCached/isCachedMemory/!isCachedFile/length()==-1/初始位置 0`。
- 顺序读 `read/readByte/readUnsignedByte`；`readInt` 大端 `0x01020304`、`setByteOrder(LITTLE_ENDIAN)` 后 `0x04030201`。
- `mark()+readFully()+reset()`、`skipBytes(10)`、`flushBefore(5)` + `getFlushedPosition()`、`seek(4)` 抛 `IndexOutOfBoundsException`。

## 5. 深化补充（testDeep* 契约覆盖，与 JDK-8 侧同源）
追加 10 个 testDeep 方法（~120 条精确断言）：构造 null → 精确 `IllegalArgumentException`；缓存状态矩阵 isCached/isCachedMemory/!isCachedFile；BE/LE 双字节序全基元读精确值；EOF 精度（剩 1 字节 readShort → 精确 `EOFException`）；边界负例全部 t.getClass()==X.class（null 数组 NPE、越界 IOOBE、setBitOffset IAE）；嵌套 mark/reset（无 mark → IOException，入 flushed 区 → 精确 `javax.imageio.IIOException`）；flushBefore/seek 边界；skipBytes 返回值（跨 EOF 返回 n、负数 IOOBE）；readBit/readBits 跨字节；close 幂等（二次 close → IOException("closed")，close 后各操作 IOException）。JDK-27 与 JDK-8 行为一致（javap 双侧公开 API 相同）。
