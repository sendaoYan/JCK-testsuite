# javax.imageio.stream.ImageInputStreamImpl — DESIGN

## 1. API 功能
`ImageInputStreamImpl` 是实现 `ImageInputStream` 的抽象基类（JDK-8 上还声明了 `protected void finalize()`，JDK-27 起移除），替子类完成字节序解释、缓冲、mark/reset、丢弃、关闭等公共逻辑，只要求子类实现 `read()` 与 `read(byte[],int,int)` 两个抽象方法。方法分组：
- 抽象：`read()`、`read(byte[],int,int)`。
- 字节序：`setByteOrder/getByteOrder`。
- 派生读：`read(byte[])`、`readBytes(IIOByteBuffer,int)`（len<0 先于 buf==null 检查）、`readBoolean/.../readUTF`、8 个 `readFully`。
- 位置/位：`getStreamPosition`、`getBitOffset/setBitOffset`（0..7）、`readBit/readBits`（0..64）、`length()`（默认 `-1L`）、`skipBytes(int/long)`（可向后跳、可越过流尾）、`seek`（不按 length 限界，只按 flushedPos）。
- 标记/丢弃：`mark/reset`（栈式，恢复字节位 + 位偏移；reset 回已 flush 标记抛 `IIOException`）、`flushBefore/flush/getFlushedPosition`、`isCached*`（默认全 false）、`close`（幂等性：二次 close 抛 `IOException("closed")`）。
- 受保护状态：`byteOrder/streamPos/bitOffset/flushedPos` + `protected final void checkClosed()`。

## 2. Java 用户如何使用
子类（如 `FileImageInputStream`、`MemoryCacheImageInputStream`）继承并实现两个抽象 read；用户面向 `ImageInputStream` 接口调用。自定义最小子类只需按 `streamPos` 读底层数组即可免费获得全部派生行为。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/ImageInputStreamImpl.java`：本类源码（readShort/readInt 用 byteBuf 批量读、readUTF 强制 BIG_ENDIAN 并在异常路径恢复、seek 只检查 `pos < flushedPos`）。
- `jdk/src/share/classes/javax/imageio/stream/FileImageInputStream.java`、`MemoryCacheImageInputStream.java`：两个标准子类，实现 `read/read(byte[],int,int)/length`。
- `jdk/test/javax/imageio/stream/ReadFullyTest.java`：readFully 各数组重载的字节序与符号扩展。
- `jdk/test/javax/imageio/stream/ShortStreamTest.java`：定长读数据不足 → `EOFException`。
- `jdk/test/javax/imageio/stream/ReadBytesIIOByteBuffer.java`：`readBytes(iiob, -1)` → `IndexOutOfBoundsException`。

## 4. 测试用例设计 + 易错规范细节（`ImageInputStreamImplTest` 各 test 方法）
- 白盒：protected 字段四件套、`checkClosed` protected final 抛 IOException、两个抽象 read、公开无参构造、JDK-8 声明 finalize（JDK-27 侧断言不声明——两侧各自断言，属版本差异）。
- 字节序（自写最小子类驱动继承行为）：short/int/long/float/double 的 BIG/LITTLE 读值互换；readFully 六种数组在 LITTLE 下的解释；`readUTF` 恒 BIG 且成功/失败（`UTFDataFormatException`、截断 EOF）路径都恢复字节序。
- 位读：MSB-first 8 位流；`readBits(8)`@bo4 跨字节 0xBC；bo4 继续 `readBits(4)` 得低半字节 0xD；`readBits(5)`@bo3 of 0xA5 = 5；`readBits(0)` 不动位；`readBits(64)` 恰 8 字节；字节读丢弃位偏移并**从当前字节开头重读**（readBit 后 readUnsignedByte 得整字节 0x80、位置只 +1）；`setBitOffset` 失败不污染现值。
- readLine：CRLF（吃 2 字节）、LF、bare CR（窥探 1 字节再回退——位置断言区分）、无终止尾、EOF null。
- EOF/越界矩阵：12 个定长读 + readBit/readBits + readFully 全部精确 `EOFException`；readFully 9 种坏 off/len IOOBE；readBytes 的 NPE/IOOBE/EOF length=-1/校验顺序（len 先于 null）；`seek(-1)`、seek 越长度合法；skipBytes 越尾合法、向后跳在 flushedPos 之上合法、pos0 处 -1 抛 IOOBE；flushBefore 三向越界。
- mark/reset：无 mark no-op；嵌套 LIFO；恢复位偏移；flushBefore 越过标记后 reset 抛 `IIOException` 且 flushedPos 不变。
- close 契约：16 个操作实测（读/位置/位/seek/flush/skip/reset/二次 close → IOException；length/getFlushedPosition/isCached*/getByteOrder/mark（静默吞异常）→ 不抛）。
- 默认实现（第二个不覆盖 length/isCached 的子类）：`length()==-1`、isCached 三连 false、其余基类行为照常。
- 易错点：hex 字面量 `0xCDEL` 是 0xCDE 不是 0xCD（L 是后缀）；readBit 单字节内位置回退（pos 不进位直到第 8 位）；裸 CR 行终止后位置比"行尾+1"多 1（窥探）。
