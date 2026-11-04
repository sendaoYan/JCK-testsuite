# java.io.DataOutputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐 write 参数校验（NPE/IOBE）、size() 计数、writeBytes/writeChars
> 字节布局、writeUTF 长度前缀与 >65535→UTFDataFormatException。

## 1. API 提供了哪些功能

- **构造器**：`DataOutputStream(OutputStream)`（不立即校验 null，写时 NPE）。
- **写**：`write(int)`（低 8 位）、`write(byte[],int,int)`、`flush()`。
- **原始类型写**：`writeBoolean/writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble`。
- **文本写**：`writeBytes(String)`（每字符低 8 位）、`writeChars(String)`（每字符 2 字节大端）、`writeUTF(String)`。
- **计数**：`size()`（已写字节数）。

## 2. Java 用户如何使用

- 与 `DataInputStream` 成对做二进制序列化；`size()` 返回已写字节数；`writeUTF` 写入 2 字节长度前缀 + 修改 UTF-8。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** + `src/share/classes/java/io/DataOutputStream.java`。
- **实现源码**：`write(int)` 存低 8 位；`write(byte[],off,len)` 委托底层 `out.write`；`writeUTF` 编码长度 >65535 → `UTFDataFormatException`。

## 4. 测试用例设计（映射到 DataOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器 null：不立即抛、写时 NPE | testConstructorNull |
| P0 | write(int) 低 8 位 | testWriteInt |
| P0 | write(byte[],off,len) 校验（NPE/IOBE） | testWriteArrayValidation |
| P0 | 9 种原始类型与 DataInputStream 往返 | testPrimitivesRoundTrip |
| P0 | size() 各写类型宽度累加（含 writeUTF 2+len） | testSize |
| P1 | writeBytes 低 8 位、writeChars 大端 2 字节/字符 | testWriteBytesChars |
| P0 | writeUTF 长度前缀 + >65535→UTFDataFormatException | testWriteUTF |
| P1 | flush 委托到底层 | testFlush |

## 关键规范细节（易错，已锁入断言）

- `writeUTF` 的 2 字节长度是**编码后的字节数**（非字符数）；编码字节数 >65535 抛 UTFDataFormatException。
- 构造器不立即校验 null。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
- JDK-27 侧同源 + 反射白盒。

## 深化追加（契约级，批次 ioC）

新增维度清单（testExactLayouts / testTruncation / testSizeContract / testFloatDoubleBits / testWriteUTFBoundary / testFlushPropagation / testCloseSemantics / testWriteAfterCloseFileBacked / testWriteUTFSerializationInterop）：

1. **逐 writeXxx 边界字节精确**：Byte/Short/Int/Long MIN+MAX、'\u0000'/'￿'、0.0/-0.0/NaN/±Inf 的完整 82 字节镜像逐字节点检（如 writeInt MAX → 7F FF FF FF、writeFloat NaN → 7F C0 00 00、writeDouble +Inf → 7F F0 00*6）。
2. **writeXxx(int) 截断**：writeByte(0x1234)→34、writeShort/Char(0x12345678)→56 78。
3. **size() 全操作矩阵**：write(b,off,len)=len、write(b[])=len、writeUTF("")=2、writeUTF("中")=5、writeBytes=每字符 1、writeChars=每字符 2；flush/close 均不清零；写穿透（sink 字节数 == 计数器）。
4. **IEEE 754 位直通**：writeFloat(Float.intBitsToFloat(0x12345678)) 逐字节 12 34 56 78；writeDouble 同（0x0123456789ABCDEF）；读回位级相等。
5. **writeUTF 编码长度边界**：65535 ASCII 合法（前缀 FFFF）+ 之后可继续写；65536 ASCII / 21846 中文（65538）/ 32768 NUL（65536）→ UTFDataFormatException；21845 中文恰 65535 合法。
6. **flush 传播探针**（CountingSink 计数）：每次 flush() 恰传一次；close 前 flush 一次 + close 一次；第二次 close 不抛（下沉 close 次数双侧不同，只断 ≥1）。
7. **close 语义**：双 close 幂等；close 前数据已落 sink；计数器可读。
8. **文件 sink close 后写**：IOException "Stream Closed"（双侧实测一致）+ 文件长度不变（被拒写未落盘）。
9. **writeUTF 与序列化互操作**：ObjectOutputStream.writeUTF("utf") 产生 AC ED 00 05 77 05 + {00 03 'u' 't' 'f'}，块数据载荷恰为 DataOutput.writeUTF 帧，可用 DataInputStream.readUTF 读回（字节级证明两者共享修改版 UTF-8 编码）。

负例矩阵（双侧实测）：

| 操作 | 异常 | 消息子串 |
|---|---|---|
| writeUTF 编码 >65535 | UTFDataFormatException | too long: 65536/65538 bytes（JDK-27 含预览，同子串） |
| writeUTF(null)/write(null) | NullPointerException | — |
| write(b,off,len) 越界 | IndexOutOfBoundsException | — |
| close 后写（文件 sink） | IOException | Stream Closed |

版本差异（实测）：writeUTF 超长消息 JDK-27 追加字符串预览；FilterOutputStream 第二次 close 在 JDK-8 会再次传播 close 到 sink（closes=2）、JDK-27 幂等不再传播（closes=1）——断言只锁 ≥1 与不抛。javap 双侧方法集一致；JDK-27 侧另测 OutputStream.nullOutputStream() sink（JDK-11+），见 JDK-27 DESIGN。

产出：`DataOutputStreamTest.java` 21 → 86 断言（jtreg Passed）。
