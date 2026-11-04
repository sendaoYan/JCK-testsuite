# java.io.DataInputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐每个 readXxx 在流末尾的 EOFException、readFully 的
> 精确填充/短读 EOF/len<0 IOBE/null NPE、skipBytes 实际跳过数、readUTF 修改 UTF-8 与
> null 往返、static readUTF、readLine、read(byte[],off,len) 校验。

## 1. API 提供了哪些功能

- **构造器**：`DataInputStream(InputStream)`（不立即校验 null，读时 NPE）。
- **原始类型读**：`readBoolean/readByte/readUnsignedByte/readShort/readUnsignedShort/readChar/readInt/readLong/readFloat/readDouble`。
- **批量读**：`readFully(byte[])`、`readFully(byte[],int,int)`、`read(byte[])`、`read(byte[],int,int)`、`skipBytes(int)`。
- **文本**：`readUTF()`（修改 UTF-8）、`static readUTF(DataInput)`、`readLine()`（弃用）。

## 2. Java 用户如何使用

- 与 `DataOutputStream` 成对做二进制往返；读越界时依赖 `EOFException` 判定数据截断。
- 依赖 `readUnsignedByte/Short` 读取 0..255/0..65535；`readFully` 一次性精确取满。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** + `src/share/classes/java/io/DataInputStream.java`。
- **实现源码**：`readFully(byte[],off,len)` 仅校验 `len<0→IOBE`，循环 `in.read` 返回负 → `EOFException`；`skipBytes` 返回实际跳过数（可能小于 n）。

## 4. 测试用例设计（映射到 DataInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器 null：不立即抛、读时 NPE | testConstructorNull |
| P0 | 9 种原始类型与 DataOutputStream 往返 | testReadPrimitivesRoundTrip |
| P0 | readUnsignedByte/Short 0..255/0..65535 | testUnsignedReads |
| P0 | 每种 readXxx 空流→EOFException；截断→EOFException | testEOFExceptions |
| P0 | readFully：精确填充、短读 EOF、len<0 IOBE、null NPE | testReadFully |
| P1 | skipBytes 实际跳过数/越过末尾/EOF 返回 0 | testSkipBytes |
| P0 | readUTF 空/ASCII/非 ASCII/null 往返 + static readUTF | testReadUTF |
| P1 | readLine 处理 \n、\r\n、EOF | testReadLine |
| P1 | read(byte[],off,len) 校验（NPE/IOBE） | testReadArrayValidation |

## 关键规范细节（易错，已锁入断言）

- 构造器不立即校验 null；`new DataInputStream(null)` 合法，首次 read 才 NPE。
- `readFully(byte[],off,len)` 只校验 `len<0`；null/负 off 由底层 `in.read` 抛 NPE/IOBE。
- `skipBytes(n)` 返回实际跳过数（读到 EOF 可能小于 n）。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；文本用显式 US-ASCII；headless 安全。
- JDK-27 侧同源 + 反射白盒。

## 深化追加（契约级，批次 ioC）

新增维度清单（对应新增方法 testBoundaryRoundTrip / testByteOrderExact / testReadUTFHandcrafted / testUTFLengthBoundary / testReadFullyContract / testSkipBytesContract / testReadLineContract / testClosedStream / testStreamBasics / testStaticReadUTFOverRaf）：

1. **边界值往返矩阵**：Byte/Short/Integer/Long MIN+MAX、'\u0000'/'￿'、+0.0/-0.0（位级 0x80000000/0x8000000000000000）、NaN（isNaN）、±Inf，+ write(0x00) 后 read()。
2. **大端序精确**：readInt {12 34 56 78}→0x12345678、readShort/readUnsignedShort/readChar/readLong 8 字节、readFloat {40 20 00 00}→2.5f、readDouble {C0 04 ..}→-2.5、{7F C0 00 00}→NaN。
3. **readUTF 手工解码矩阵**（独立于 writeUTF 实现）：ASCII {0005..}、两字节 {C3 A9}→é、三字节中文 {E4 B8 AD}→中、修改版 UTF-8 NUL {C0 80}→U+0000、CESU-8 孤立代理 {ED A0 80}→U+D800（不校验代理）、长度 0、尾字节保留；非法/截断见负例表。
4. **writeUTF 编码长度边界**：65535 字节恰合法（前缀 FFFF，65537 总长）可完整读回；65536 ASCII、32768 NUL（=65536 字节）→ UTFDataFormatException；32767 NUL 合法（65536 总长）；代理对 = 6 字节 CESU-8 往返。
5. **readFully 契约补全**：短流抛 EOF 后**已传前缀保留**；readFully(b,off,len) 精确区域；len==0 在 EOF 上为 no-op 不抛；off+len 越界 IOBE；readFully((byte[])null) NPE。
6. **skipBytes 负/零**：skipBytes(-5)=0 且位置不动；0=0；部分跳过后精确续读。
7. **readLine 行界矩阵**："x\r\r\ny\r" → x、""（\r\n 合一）、y、null；空流 null；"\n\n" 两个空行；无终结符尾行；"\r\n" 单空行。
8. **close 后行为**（FileInputStream 支撑，close 可观察）：read/readInt/readLine/available → IOException "Stream Closed"（实测双侧一致）；skipBytes 类型级断言（消息双侧不同）。
9. **委托与双层**：read() 无符号 0..255、read(byte[]) 批量、available 委托、skip(1) EOF 0、双层 DataInputStream 帧完整。
10. **static readUTF 跨实现**：DataInputStream.readUTF(RandomAccessFile) 解码 + 指针推进 + EOF。

负例矩阵（双侧实测）：

| 操作 | 异常 | 消息子串（JDK-8 / JDK-27 一致） |
|---|---|---|
| readUTF 前导续字节 {00 01 80} | UTFDataFormatException | malformed input around byte 0 |
| readUTF 断链 {00 02 C3 28} | UTFDataFormatException | malformed input around byte 2 |
| readUTF 正文短于声明 | EOFException | — |
| readUTF 声明 65535 仅 1 字节 | EOFException | — |
| writeUTF 编码 >65535 字节 | UTFDataFormatException | too long: 65536 bytes（JDK-27 消息多出字符串预览，含相同子串） |
| readFully 越界 off+len | IndexOutOfBoundsException | — |
| readFully((byte[])null) | NullPointerException | — |
| close 后 read/readInt/readLine/available | IOException | Stream Closed |
| close 后 skipBytes | IOException | JDK-8 "Stream Closed" / JDK-27 "Bad file descriptor"（只断类型） |

版本差异（实测）：close 后 skipBytes 消息不同（见上）；writeUTF 超长消息 JDK-27 追加字符串预览（共同子串 "too long: N bytes"）。javap 双侧公开方法集一致（无 JDK-27 特有新方法；JDK-27 侧另测 JDK-9+ 继承的 readAllBytes/readNBytes/transferTo，见 JDK-27 DESIGN）。

产出：`DataInputStreamTest.java` 25 → 134 断言（jtreg Passed）。
