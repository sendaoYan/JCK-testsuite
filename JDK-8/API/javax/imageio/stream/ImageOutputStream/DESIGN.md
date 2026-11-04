# javax.imageio.stream.ImageOutputStream — DESIGN

## 1. API 功能
`ImageOutputStream` 同时扩展 `ImageInputStream` 与 `java.io.DataOutput`（双侧 JDK 均无默认方法），在可随机读的基础上增加可随机写。方法分组：
- 原始写：`write(int)`、`write(byte[])`、`write(byte[],int,int)`、`writeBoolean/writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble/writeBytes(String)/writeChars(String)/writeUTF(String)`。
- 数组写：`writeShorts/writeChars(char[])/writeInts/writeLongs/writeFloats/writeDoubles`（各带 off/len，越界抛 `IndexOutOfBoundsException`）。
- 位写：`writeBit(int)`（参数掩码到 1 位）、`writeBits(long,int)`（numBits 0..64，越界 IAE；跨字节时保护既有位）。
- 丢弃：`flushBefore(long)`（把前缀刷入底层输出）。
- 异常契约：`writeUTF` 编码长度 >65535 抛 `UTFDataFormatException`；`writeBits` 坏 numBits 抛 `IllegalArgumentException`；close 后写抛 `IOException`。

## 2. Java 用户如何使用
用户用 `new MemoryCacheImageOutputStream(new ByteArrayOutputStream())` 或 `ImageIO.createImageOutputStream(...)`。典型链：按格式写字段（`writeShort/writeInt/writeUTF`，字节序由 `setByteOrder` 控制）→ `writeBits` 位打包 → `seek` 回填 → `flushBefore(pos)` 分段落底层 → `close()` 全量落盘 → 从底层流 `toByteArray()` 取字节。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/ImageOutputStreamImpl.java`：`writeShort/writeInt/writeLong/writeUTF/writeShorts/...` 按 `byteOrder` 编码后调 `write(byte[],int,int)`。
- `jdk/src/share/classes/javax/imageio/ImageWriter.java`：`writeInt/writeShort/writeUTF` 组合写图像头。
- `jdk/test/javax/imageio/stream/BitPadding.java`（4430395）：`write(int)` 必须把未写满的位用 0 补齐（writeBit(1)+write(96) → 0x80,0x60）。
- `jdk/test/javax/imageio/stream/WriteBitsTest.java`（4507868）：`writeBits` 的 streamPos/bitOffset 记账与 `read` 变体复位 bitOffset。
- `jdk/test/javax/imageio/stream/MemoryCacheImageOutputStreamTest.java`：写后 `seek` 散点回写、`flushBefore` 分段刷出。

## 4. 测试用例设计 + 易错规范细节（`ImageOutputStreamTest.testSignatures/testBehavior`）
- 反射：write×3 重载、writeChars×2 重载（String 与 char[]）、每个 writeXxx 的参数/返回/throws；`java.io.DataOutput` 的全部方法都被本接口重新暴露。
- 行为（载体 `MemoryCacheImageOutputStream`）：writeShort/writeInt/writeLong 的 BIG/LITTLE 字节布局逐字节断言；跨序互换（LITTLE 写 0x12345678 → BIG 读 0x78563412）；1.5f/2.25 的位模式。
- `writeBytes(String)` 只写每字符低 8 位（"AŁB"→41 41 42）；`writeChars(String)` 双字节且按字节序布局。
- `writeUTF`：2 字节长度前缀恒大端（即使 LITTLE）；modified-UTF8 宽度（NUL=2、U+0800+=3）；round-trip；utflen 65536（ASCII 与 3 字节字符两路）抛 `UTFDataFormatException` 且不写字节；65535 边界可往返。
- 位写：`writeBit(1)+write(96)` → 80 60（补 0）；writeBit 掩码（writeBit(3) 只存 1 位）；`writeBits(1,2)+writeBits(-1,5)` 拼 0xBF；writeBits 中途写既有字节保留邻位（FF→F0，跨字节 F0 0F）；`writeBits(_,0)` no-op；`writeBits(-1/65)` IAE；writeByte 前隐式 flushBits 补 0。
- 数组写：6 个数组 writer 的坏 off/len 全部 IOOBE；零长度不写字节；LITTLE/BIG round-trip 经 readFully 数组验证。
- seek 回写：`seek(2)+writeShort` 原地覆盖、邻字节不动；`flushBefore(2)` 底层流恰收到前 2 字节；`flushBefore` 低于/超出位置的 IOOBE；close 后 write/writeInt/writeBit/writeUTF IOException。
- 易错点：断言底层字节前必须先 `seek(length)+flush()`（flush 会推高 flushedPos，之后不能 seek 回低处——先做完读回断言再 flush）；hex 比较注意 byte 符号（& 0xFF）。
