# javax.imageio.stream.MemoryCacheImageOutputStream — DESIGN

## 1. API 功能
`MemoryCacheImageOutputStream` 是 `ImageOutputStreamImpl` 的具体子类，把写出内容累积在内存 `MemoryCache` 中，`flush()` 时一次性推给底层 `OutputStream`。方法分组：
- 构造：`MemoryCacheImageOutputStream(OutputStream stream)`；`stream == null` 抛 `IllegalArgumentException`（消息 `"stream == null!"`）。
- 写：`write(int)`、`write(byte[],int,int)`；`writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble/writeBoolean/writeBytes/writeChars/writeUTF` 与数组版 `writeShorts/writeChars/writeInts/writeLongs/writeFloats/writeDoubles` 全部经基类按 `byteOrder` 展开。
- 读回：本类同时实现 `read()` 族（内存缓存可回读），`seek` 后可 `read`。
- 位写：`writeBit`、`writeBits(long,int)`（写入 numBits 个低位，MSB 优先填充）。
- 缓存状态矩阵：`isCached()`=true、`isCachedMemory()`=true、`isCachedFile()`=false。
- 位置/丢弃：`seek`（越过当前长度时洞填 0）、`mark/reset`、`flush()`（推送到底层流）、`flushBefore`、`close`。
- `length()` 返回当前已写长度（缓存长度），随写增长。

## 2. Java 用户如何使用
典型链：`new MemoryCacheImageOutputStream(baos)` → `setByteOrder` → 写头/写体 → `length()` 随写增长 → `seek` 回填 → `flush()` 推给底层 → `close()`。`ImageIO.createImageOutputStream(OutputStream)` 在 `useCache==false` 时返回本类。写完通常再 `baos.toByteArray()` 取结果。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/MemoryCacheImageOutputStream.java`：`write` 经 `cache.write`，`flush()` 调 `cache.writeToStream(stream)` 后 `cache.disposeBefore(length)`；`length()` 返回 `cache.getLength()`。
- `com/sun/imageio/plugins/jpeg/JFIFMarkerSegment.java`：JPEG 写路径经 `ImageIO.createImageOutputStream`（本类为默认无缓存实现）写 marker 段。
- `jdk/test/javax/imageio/stream/StreamFlush.java`：验证 flush/flushBefore 时底层流收到的字节区间。
- `jdk/test/javax/imageio/stream/WriteBitsTest.java`（JDK-27 侧同名）：writeBit/writeBits 填充契约。

## 4. 测试用例设计 + 易错规范细节（`MemoryCacheImageOutputStreamTest` testDeep*）
- 构造与状态矩阵：null → 精确 `IllegalArgumentException`；`isCached/isCachedMemory/!isCachedFile`；默认 BE；`length()==0`。
- 写基元：全类型写出后 `length()==41` 且对 `ByteArrayOutputStream` 落盘字节逐位断言（boolean/byte/short/char/int/long/float/double/writeBytes/writeChars/writeUTF("hi")=4 字节修正 UTF）。
- 双字节序 round-trip：LE 下 int/long/short/double/float/char/UTF 经 `MemoryCacheImageInputStream` 读回精确值。
- 数组 round-trip：`writeShorts/writeChars/writeInts/writeLongs/writeFloats/writeDoubles` 共 58 字节，`readFully` 各数组精确值。
- flush 语义：`flush()` 后 `getFlushedPosition()==length`；`flushBefore(n)` 推送 `[0,n)`，`getFlushedPosition` 精确推进；`seek(<flushedPos)` → 精确 `IndexOutOfBoundsException`。
- seek/洞填 0：`seek(5)` 不增长，写后 `length` 跳到 6，中间字节读回为 0；`seek` 复位 `bitOffset`。
- 位写：3 bit + flush → `0xA0`；`writeBits(0b101,3)+writeBits(0b010111,6)+writeBits(0b1,7)` → `0xAB 0x81`；非对齐位偏移 `writeBits` RMW 保留未触位（`0xFF`+位 2 起 2 个 0 → `0xCF`）；`writeBits(x,0)` no-op；numBits 65/-1 → 精确 `IllegalArgumentException`；`writeBits(all1,64)` 满 64 位。
- 边界负例（精确类）：`write(null,0,1)`/`writeShorts(null,...)` → `NullPointerException`（null 数组在长度检查前解引用）；len/off 越界 → `IndexOutOfBoundsException`。
- EOF 读：到长度后 `read()` 返回 -1；剩 1 字节 `readShort` → 精确 `EOFException`。
- close 状态：第二次 `close()` → 精确 `NullPointerException`（本类特有，与输入侧的 IOException("closed") 不同，实现里 `close` 后 `stream==null`）；close 后 `write/read/seek/getStreamPosition/flushBefore` → 精确 `IOException`；`length()` close 后不抛、内部吞异常返回 -1。
- 版本差异：JDK-8 与 JDK-27 公开 API（javap 对照）完全一致，本类行为一致。

## 5. 未写入 Javadoc 的实测行为
- 第二次 `close()` 抛 NPE 而非 IOException（`close()` 把 `stream` 置 null，基类二次 `checkClosed` 前先解引用）。
- `length()` close 后返回 -1 不抛（catch IOException 返回 -1）。
- seek 越过当前长度后写入，中间空洞读回为 0（缓存实现 pad 0）。
- `writeBits` 写入的是 value 的低 numBits 位，非高位。
- null 数组先于越界检查抛 NPE（长度检查表达式 `off + len > s.length` 对 null 解引用）。
