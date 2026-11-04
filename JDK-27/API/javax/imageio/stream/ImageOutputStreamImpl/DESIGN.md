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
