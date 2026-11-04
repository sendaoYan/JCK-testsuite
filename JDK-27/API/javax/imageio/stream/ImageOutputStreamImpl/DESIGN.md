# javax.imageio.stream.ImageOutputStreamImpl — DESIGN

## 1. API 功能
`ImageOutputStreamImpl` 继承 `ImageInputStreamImpl` 并实现 `ImageOutputStream`，为输出流提供所有 `writeXxx` 的编码逻辑，子类只需实现 `write(int)` 与 `write(byte[],int,int)`（及继承自输入侧的 `read()`、`read(byte[],int,int)`）。方法分组：
- 抽象：`write(int)`、`write(byte[],int,int)`。
- 派生写：`write(byte[])`、`writeBoolean/writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble/writeBytes/writeChars/writeUTF`、`writeShorts/writeChars(char[])/writeInts/writeLongs/writeFloats/writeDoubles`。
- 位写：`writeBit(int)`、`writeBits(long,int)`；受保护 `flushBits()`。
- 其余继承 `ImageInputStreamImpl`：`readByte/readInt/readFully/seek/mark/reset/flushBefore/...`。

## 2. Java 用户如何使用
子类（如 `FileImageOutputStream`、`MemoryCacheImageOutputStream`）继承并实现 `write(int)`/`write(byte[],int,int)`（实现里先调 `flushBits()`）。用户面向 `ImageOutputStream` 接口写数据后 `seek(0)` 读回校验。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/FileImageOutputStream.java`：`write(int)` 先 `flushBits()` 再 `raf.write`；`write(byte[],int,int)` 同理。
- `src/java.desktop/share/classes/javax/imageio/stream/MemoryCacheImageOutputStream.java`：`write(int)` 先 `flushBits()` 再 `cache.write`。
- `test/jdk/javax/imageio/stream/WriteBitsTest.java`：验证 `writeBits` 的跨字节填充与 `flushBits` 语义。

## 4. 测试用例设计 + 自动化实现（`ImageOutputStreamImplTest.testBlackBox`）
- 内联最小子类（实现 `read/read(byte[],int,int)/write/write(byte[],int,int)/length`），写 15 类数据（`writeByte/writeInt/writeLong/writeFloat/writeDouble/writeBoolean/writeChar/writeUTF/writeBytes/writeChars` + `writeShorts/writeInts/writeLongs/writeFloats/writeDoubles`），断言共 69 字节。
- `seek(0)` 逐个读回全部值，覆盖 `readBytes`。
- `mark/reset`、`flush()`（位置 0）、`seek(10)` + `flushBefore(10)`、`seek(9)` 抛 `IndexOutOfBoundsException`。

## 5. 深化补充（第二轮：契约级覆盖，自写 checked 最小子类）
追加 `testWhiteBoxExtra`（flushBits protected final、write(int)/write(byte[],int,int) abstract、write(byte[]) 具体、无 finalize、继承 protected 状态）与行为方法：
- `testWriteByteOrders`：全部写宽度的 BIG/LITTLE 逐字节布局（含 1.5f=3FC00000、2.25 位模式）、同序读回、跨序互换、writeBytes 低 8 位截断、writeChars(String) 双序、setByteOrder(null)。
- `testWriteUTFContract`：前缀恒大端、modified-UTF8 宽度记账、round-trip、utflen 65536 两路 UTFDataFormatException 且零字节写出、65535 边界。
- `testBitWriting`：补 0（80 60）、掩码、writeBits(10,3)@bo3 两次接续（OpenJDK 4507868 场景 08→09 00）、(-1,64)、保护邻位（FF→F0、跨字节 F0 F0 0F）、numBits IAE、writeBit(-1) 存 1。
- `testArrayWrites`：6 个数组 writer 坏参 IOOBE + 零长度 no-op + LITTLE 逐字节与 round-trip + write(byte[],off,len) 窗口。
- `testRandomAccessFlushClose`：seek 原地覆盖、读写交错、flushBefore 三向越界、close 后 10 项 IOException（含二次 close）。
子类 write 内必须自行 checkClosed（基类不代查）——测试用 checked 子类验证该模板契约。
