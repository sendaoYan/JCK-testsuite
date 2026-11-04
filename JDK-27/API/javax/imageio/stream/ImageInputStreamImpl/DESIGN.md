# javax.imageio.stream.ImageInputStreamImpl — DESIGN

## 1. API 功能
`ImageInputStreamImpl` 是实现 `ImageInputStream` 的抽象基类，替子类完成字节序解释、缓冲、mark/reset、丢弃、关闭等公共逻辑，只要求子类实现 `read()` 与 `read(byte[],int,int)` 两个抽象方法。方法分组：
- 抽象：`read()`、`read(byte[],int,int)`。
- 字节序：`setByteOrder/getByteOrder`。
- 派生读：`read(byte[])`、`readBytes`、`readBoolean/readByte/readUnsignedByte/readShort/readUnsignedShort/readChar/readInt/readUnsignedInt/readLong/readFloat/readDouble/readLine/readUTF`、8 个 `readFully`。
- 位置/位：`getStreamPosition`、`getBitOffset/setBitOffset`、`readBit/readBits`、`length()`（默认 `-1L`）、`skipBytes(int/long)`、`seek`。
- 标记/丢弃：`mark/reset/flushBefore/flush/getFlushedPosition/isCached/isCachedMemory/isCachedFile/close`；受保护字段 `byteOrder/streamPos/bitOffset/flushedPos`。

## 2. Java 用户如何使用
子类（如 `FileImageInputStream`、`MemoryCacheImageInputStream`）继承并实现两个抽象 read；用户面向 `ImageInputStream` 接口调用。自定义子类示例：实现 `read()` 基于 `streamPos` 读底层数组，实现 `read(byte[],int,int)` 批量拷贝，其余 `readByte/readInt/readFully/mark/reset/seek/flushBefore` 均由基类提供。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/stream/FileImageInputStream.java`：继承本类，实现 `read/read(byte[],int,int)/length/seek`。
- `src/java.desktop/share/classes/javax/imageio/stream/MemoryCacheImageInputStream.java`：继承本类，`read()` 经 `MemoryCache.loadFromStream`。
- `test/jdk/javax/imageio/stream/ReadFullyTest.java`：验证 `readFully` 各数组重载的字节序与符号扩展。

## 4. 测试用例设计 + 自动化实现（`ImageInputStreamImplTest.testBlackBox`）
- 内联最小子类（仅实现 `read/read(byte[],int,int)/length`），用 `DataOutputStream` 编码固定序列，测试继承的 `readByte/readShort/readInt/readLong/readFloat/readDouble/readBoolean/readChar/readUTF` 与 `readUnsignedByte/readUnsignedShort/readUnsignedInt`（固定偏移读回）。
- `mark/reset`、8 个 `readFully` 重载（大端解释）。
- 位操作 `setBitOffset/readBit/readBits`；`skipBytes(int/long)`、`flushBefore/flush/getFlushedPosition` 及 `seek` 越界抛异常。

## 5. 深化补充（第二轮：契约级覆盖，最小子类驱动全部继承行为）
追加 `testWhiteBoxExtra`（protected 字段四件套、checkClosed protected final、两个抽象 read、无参构造、**JDK-27 不再声明 finalize()**——JDK-8 侧仍声明，两侧各自断言，为实测版本差异）与行为方法：
- `testByteOrderValues`：short/int/long/float/double 的 BIG/LITTLE 互换；readFully 六数组 LITTLE 解释；readUTF 恒 BIG 且失败路径恢复字节序。
- `testBitOps`：MSB-first 位流、跨字节、字节读丢弃位偏移并从当前字节开头重读（0x80 整字节）、setBitOffset 失败不污染。
- `testReadLine`：全终止符矩阵 + bare CR 窥探回退位置。
- `testBoundsAndEof`：EOF 12 项 + readFully 9 种坏参 + readBytes 校验顺序（len 先于 null）+ seek/skipBytes/flushBefore 三向（skipBytes 允许越过流尾与向后跳）。
- `testMarkReset`：嵌套 LIFO、位偏移恢复、reset 回已 flush 标记 → javax.imageio.IIOException。
- `testCloseContract`：16 项 close 后行为矩阵（length/getFlushedPosition/isCached*/getByteOrder/mark 不受影响；二次 close 抛 IOException("closed")）。
- `testDefaults`：不覆盖 length/isCached 的子类 → length()==-1、isCached 三连 false。
