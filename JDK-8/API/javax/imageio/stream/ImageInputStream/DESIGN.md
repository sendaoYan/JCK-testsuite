# javax.imageio.stream.ImageInputStream — DESIGN

## 1. API 功能
`ImageInputStream` 是 `java.io.DataInput` + `java.io.Closeable` 的子接口（双侧 JDK 均无默认方法，全部 abstract），定义可随机读（seek/mark/reset/skip）的二进制输入流。方法分组：
- 字节序：`setByteOrder(ByteOrder)`（容忍 null，原样存储）、`getByteOrder()`（默认 `BIG_ENDIAN`）。
- 原始读：`read()`、`read(byte[])`、`read(byte[],int,int)`、`readBytes(IIOByteBuffer,int)`、`readBoolean/readByte/readUnsignedByte/readShort/readUnsignedShort/readChar/readInt/readUnsignedInt/readLong/readFloat/readDouble/readLine/readUTF`。
- 批量读：8 个 `readFully` 重载（byte/short/char/int/long/float/double 数组）。
- 位置/位：`getStreamPosition()`、`getBitOffset()`、`setBitOffset(int)`（仅 0..7）、`readBit()`（MSB-first）、`readBits(int)`（0..64）、`length()`、`skipBytes(int/long)`、`seek(long)`。
- 标记/丢弃：`mark()`、`reset()`（LIFO 栈，恢复字节位 + 位偏移）、`flushBefore(long)`、`flush()`、`getFlushedPosition()`、`isCached/isCachedMemory/isCachedFile()`、`close()`。
- 异常契约：EOF 处 `readXxx`（定长）抛 `EOFException`；`read()`/`read(byte[])` 返回 -1；`readUTF` 载荷损坏抛 `UTFDataFormatException`；越界 `seek/flushBefore/readFully/readBytes` 抛 `IndexOutOfBoundsException`；`setBitOffset/readBits` 参数越界抛 `IllegalArgumentException`；`reset` 回已 flush 的标记抛 `javax.imageio.IIOException`；close 后多数操作抛 `IOException("closed")`。

## 2. Java 用户如何使用
用户不直接实现此接口，而是 `new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))` 或 `ImageIO.createImageInputStream(...)`。典型链：`setByteOrder(LITTLE_ENDIAN)` → `readByte/readShort/readInt/...` 解码 → `readBytes(buf,n)` 零拷贝段 → `seek(0)`/`mark()+reset()` 回退 → `readBit/readBits` 位级解析 → `flushBefore(pos)` 释放前缀 → `close()`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + test/jdk）
- `jdk/src/share/classes/javax/imageio/stream/ImageInputStreamImpl.java`：接口的参考实现（`readShort/readInt/readUTF/readFully/mark/reset/seek/flushBefore` 的全部语义都定义在这里）。
- `jdk/src/share/classes/javax/imageio/ImageReader.java`：大量 `readInt/readUnsignedShort/readUTF` 组合解析图像头部。
- `jdk/test/javax/imageio/stream/ShortStreamTest.java`（8074954）：多字节类型在数据不足时必须抛 `IOException`（EOF）。
- `jdk/test/javax/imageio/stream/ReadUnsignedIntTest.java`：`readUnsignedInt` 以 long 返回无符号 32 位值。
- `jdk/test/javax/imageio/stream/ReadBytesIIOByteBuffer.java`：`readBytes` 负长度抛 `IndexOutOfBoundsException`。

## 4. 测试用例设计 + 易错规范细节（`ImageInputStreamTest.testSignatures/testBehavior`）
- 反射：接口层级（DataInput+Closeable）、重载数（read×3、readFully×8、skipBytes×2）、每个方法的参数类型/返回类型/throws 子句（`mark()`、`getFlushedPosition()`、`isCached*` 不抛异常）。
- 行为（载体 `MemoryCacheImageInputStream`）：BIG/LITTLE 读值互换（同 4 字节 readShort 0x1234↔0x3412、readInt 0x12345678↔0x78563412）；无符号语义（0xFF→255、0xFFFF→65535、0xFFFFFFFF→4294967295L）；`setByteOrder(null)` 原样存储。
- `readUTF` 恒按网络序解码且调用后恢复原字节序（含 `UTFDataFormatException` 失败路径）。
- `readLine`：CRLF/LF/bare CR/无终止符/EOF null；bare CR 会向后窥探 1 字节再回退（位置断言）。
- 位读：MSB-first 逐位、`readBits(8)`@bitOffset4 跨字节（AB CD→0xBC）、`readBits(0)` 不动位、`readBits(64)` 恰好 8 字节、`setBitOffset(-1/8)`、`readBits(-1/65)` IAE。
- `readBytes`：offset 恒 0、每次换新数组、EOF 处 length 置 -1（不抛异常，javadoc 未写）、null buffer NPE、负长度 IOOBE、失败后位置不变。
- 越界矩阵（全部精确类断言）：`seek(-1)`、`flushBefore` 低于/超出当前位置、`readFully` 9 种坏 off/len、EOF 12 个定长读全部 EOFException、`skipBytes` 允许越过流尾（返回 n）。
- mark/reset：无 mark 时 no-op；嵌套 LIFO；恢复位偏移；`reset` 回已 flush 标记抛 `IIOException`。
- close 契约：close 后 11 类操作 IOException；二次 close 也抛 IOException；`length/getFlushedPosition/isCached*/getByteOrder/mark`（吞异常）在 close 后仍可用。
- 易错点：readBytes 在 EOF 报 length=-1；skipBytes 可跳过流尾；`mark()` 在 close 后静默；断言全部用 `getClass()==精确类`。
