# javax.imageio.stream.ImageOutputStream — DESIGN

## 1. API 功能
`ImageOutputStream` 同时扩展 `ImageInputStream` 与 `java.io.DataOutput`，在可随机读的基础上增加可随机写。方法分组：
- 原始写：`write(int)`、`write(byte[])`、`write(byte[],int,int)`、`writeBoolean/writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble/writeBytes/writeChars/writeUTF`。
- 数组写：`writeShorts/writeChars(char[])/writeInts/writeLongs/writeFloats/writeDoubles`（各带 off/len）。
- 位写：`writeBit(int)`、`writeBits(long,int)`。
- 丢弃：`flushBefore(long)`（把前缀刷入底层输出）。
- 其余继承自 `ImageInputStream`：`seek/mark/reset/readByte/readInt/readFully/...`、`getFlushedPosition/isCached/...`。

## 2. Java 用户如何使用
用户不直接 new，而是 `ImageOutputStream ios = ImageIO.createImageOutputStream(new ByteArrayOutputStream())`。典型组合链：`writeByte/writeShort/writeInt/writeLong/writeFloat/writeDouble/writeBoolean/writeChar/writeUTF/writeBytes/writeChars` + `write(byte[])` + `writeShorts/writeInts/writeLongs/writeFloats/writeDoubles` 编码 → `seek(0)` + `readByte/readShort/...` 读回校验 → `flushBefore(pos)`/`close()` 落盘 → `ByteArrayOutputStream.toByteArray()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/ImageOutputStreamImpl.java`：`writeShort/writeInt/writeLong/writeUTF/writeShorts/...` 按 `byteOrder` 编码后调用 `write(byte[],off,len)`。
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`writeInt/writeShort/writeUTF` 组合写图像头。
- `test/jdk/javax/imageio/stream/MemoryCacheImageOutputStreamTest.java`：写入 30KB 后 `seek` 散点写、`flushBefore(2*8192)` 再读回。

## 4. 测试用例设计 + 自动化实现（`ImageOutputStreamTest.testBlackBox`）
- 经 `ImageIO.createImageOutputStream` 取得实例，写 18 类数据（原始 + 数组 + `write(byte[])/write(byte[],off,len)`），断言 `length()==79`、`getStreamPosition()==79`。
- `seek(0)` 逐个读回全部写入值，覆盖 `readBytes`、`readFully`。
- `mark/reset`、`skipBytes`、`flushBefore(8)` + 底层流已收到 8 字节 + `seek(7)` 抛异常、`close()` 后底层流共 79 字节。

## 5. 深化补充（第二轮：契约级覆盖）
追加 `testSignatures`（write×3/writeChars×2 重载、逐方法签名与 throws、DataOutput 方法全量再暴露断言）与 `testBehavior`（MemoryCacheImageOutputStream 载体）：
- writeShort/writeInt/writeLong 的 BIG/LITTLE 逐字节布局 + 跨序互换 + 1.5f/2.25 位模式。
- `writeBytes(String)` 低 8 位截断（"AŁB"→41 41 42）、`writeChars(String)` 双序布局。
- `writeUTF`：长度前缀恒大端；modified-UTF8 宽度（NUL=2、U+0800+=3）与 round-trip；utflen 65536 两路抛 UTFDataFormatException 且不写字节、65535 边界可往返。
- 位写：writeBit(1)+write(96)→80 60 补 0；writeBit 掩码；writeBits 接续/保护邻位（FF→F0、跨字节 F0 0F）/(-1,64) 填满/numBits IAE/零宽 no-op；writeByte 前 flushBits 补 0。
- 6 个数组 writer 坏 off/len 全 IOOBE + 零长度 no-op + LITTLE/BIG round-trip。
- seek 原地覆盖、flushBefore 底层流可见性与三向越界、close 后写 4 项 IOException。
JDK-27 与 JDK-8 的本接口 API 完全一致（javap 双侧对照无差异）。
