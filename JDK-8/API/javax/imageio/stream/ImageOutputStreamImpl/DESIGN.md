# javax.imageio.stream.ImageOutputStreamImpl — DESIGN

## 1. API 功能
`ImageOutputStreamImpl` 继承 `ImageInputStreamImpl` 并实现 `ImageOutputStream`（两侧 JDK 均无 finalize 声明），为输出流提供全部 `writeXxx` 的编码逻辑。子类只需实现 `write(int)` 与 `write(byte[],int,int)`（及继承的 `read()`/`read(byte[],int,int)`；规范要求子类在这两个 write 开头先调 `flushBits()` 并 `checkClosed()`）。方法分组：
- 抽象：`write(int)`、`write(byte[],int,int)`；具体：`write(byte[])`。
- 派生写：`writeBoolean/writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble/writeBytes(String)/writeChars(String)/writeUTF(String)`、`writeShorts/writeChars(char[])/writeInts/writeLongs/writeFloats/writeDoubles`（各带 off/len 校验）。
- 位写：`writeBit(int)`（掩码到 1 位，不校验参数）、`writeBits(long,int)`（0..64；在已有字节中间写入时保护邻位；EOF 处补 0 字节）；`protected final void flushBits()`（把当前字节剩余位补 0 并前进）。

## 2. Java 用户如何使用
子类（如 `FileImageOutputStream`、`MemoryCacheImageOutputStream`）实现两个抽象 write（开头调 `flushBits()`）。用户面向 `ImageOutputStream` 接口写数据后 `seek(0)` 用继承的读方法验证，或 `flushBefore`/`close` 把字节推给底层流。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/ImageOutputStreamImpl.java`：writeShort/writeInt 用 byteBuf 编码后整块写；writeLong 分两个 4 字节写；writeUTF 自算 modified-UTF8 长度，>65535 抛 `UTFDataFormatException("utflen > 65536!")`；writeBits 的 prologue/whole-bytes/epilogue 三段逻辑（含 4499158/4507868/4494976 修复）。
- `jdk/src/share/classes/javax/imageio/stream/FileImageOutputStream.java`：`write(int)` 先 `flushBits()` 再 `raf.write`——标准子类模板。
- `jdk/test/javax/imageio/stream/BitPadding.java`（4430395）：`write(int)` 必须补 0 位。
- `jdk/test/javax/imageio/stream/WriteBitsTest.java`（4507868）：writeBits 记账 + read 变体复位 bitOffset。

## 4. 测试用例设计 + 易错规范细节（`ImageOutputStreamImplTest` 各 test 方法）
- 白盒：`flushBits` protected final 抛 IOException；write(int)/write(byte[],int,int) abstract、write(byte[]) 具体；public 无参构造；无 finalize；从父类继承 protected streamPos/bitOffset。
- 字节序布局（自写 checked 最小子类，write 内 checkClosed）：writeShort/writeInt/writeLong/writeFloat/writeDouble 的 BIG/LITTLE 逐字节布局（含 1.5f=0x3FC00000、2.25=0x4002000000000000 位模式）；同序读回；跨序互换；`setByteOrder(null)` 原样存储；writeBoolean/writeByte/writeChar/writeBytes("AŁB"→41 41 42)/writeChars(String) 双序布局。
- writeUTF：前缀恒大端（LITTLE 下 0002）；modified-UTF8 宽度记账（NUL=2、U+0800+=3）；round-trip；utflen 65536 两路（65536×ASCII、32768×U+0800）抛 `UTFDataFormatException` 且不写字节；65535 边界写读往返。
- 位写：writeBit(1)+write(96)→80 60（补 0）；writeBit(3)/writeBit(-1) 掩码存 1；writeBits(10,3)@bo3 的两次接续（08 → 09 00，pos/bo 记账按 OpenJDK 4507868 场景）；writeBits(-1,64) 填满 8 字节；中途写入保护邻位（FF→F0；跨字节 F0 F0 0F）；writeBits(_,0) no-op；writeBits(-1/65) IAE。
- 数组写：6 个数组 writer 坏 off/len 全 IOOBE；零长度不写字节；LITTLE 布局逐字节断言 + readFully 数组同序读回；write(byte[])/write(byte[],off,len) 窗口语义。
- 随机写/flush/close：seek+writeShort 原地覆盖；读写交错；flushBefore 三向越界 IOOBE；flushed 区之上写合法；close 后 10 类写/定位操作 IOException、二次 close IOException、length/getFlushedPosition 仍可用。
- 易错点：子类 write 忘调 `checkClosed()` 时"close 后写抛 IOException"不会生效（基类不代查）——测试用 checked 子类；float 1.5f 的位模式是 3FC00000 不是 3F800000；writeChar('Z') 占 2 字节，布局串长度要数清。
