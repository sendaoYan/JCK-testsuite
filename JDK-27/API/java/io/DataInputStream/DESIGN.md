# java.io.DataInputStream 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 提供了哪些功能

- **构造器**：`DataInputStream(InputStream)`（不立即校验 null，读时 NPE）。
- **原始类型读**：`readBoolean/readByte/readUnsignedByte/readShort/readUnsignedShort/readChar/readInt/readLong/readFloat/readDouble`。
- **批量读**：`readFully(byte[])`、`readFully(byte[],int,int)`、`read(byte[],int,int)`、`skipBytes(int)`。
- **文本**：`readUTF()`（修改 UTF-8）、`static readUTF(DataInput)`、`readLine()`（弃用）。

## 2. Java 用户如何使用

- 与 `DataOutputStream` 成对做二进制往返；读越界依赖 `EOFException`；`readUnsignedByte/Short` 读 0..255/0..65535。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** + `~/git/jdk/src/java.base/share/classes/java/io/DataInputStream.java`。

## 4. 测试用例设计（映射到 DataInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 反射白盒：继承 FilterInputStream、实现 DataInput、readInt/readLong/readUTF/readFully/readUnsignedByte 存在 | testWhiteBox |
| P0 | 构造器 null 读时 NPE；9 原始类型往返；unsigned 读 | testConstructorNull、testReadPrimitivesRoundTrip、testUnsignedReads |
| P0 | 每种 readXxx 空流/截断 EOFException | testEOFExceptions |
| P0 | readFully 精确/短读 EOF/len<0 IOBE/null NPE | testReadFully |
| P1 | skipBytes 实际跳过数 | testSkipBytes |
| P0 | readUTF 空/ASCII/非 ASCII + static readUTF | testReadUTF |
| P1 | readLine（\n、\r\n、EOF）；read 校验 | testReadLine、testReadArrayValidation |

## 关键规范细节（易错，已锁入断言）

- 构造器不立即校验 null；`readFully` 只校验 `len<0`；`skipBytes` 返回实际跳过数。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。

## 深化追加（契约级，批次 ioC）

新增维度清单（与 JDK-8 侧完全镜像 + JDK-27 特有追加，方法名一致）：

1. **边界值往返矩阵**（MIN/MAX/'\u0000'/'￿'/+0.0/-0.0 位级/NaN/±Inf）。
2. **大端序精确**（readInt {12 34 56 78} 等 10 项手写字节断言）。
3. **readUTF 手工解码矩阵**（ASCII/两字节 é/三字节 中文/修改版 NUL {C0 80}/CESU-8 孤立代理/长度 0/尾字节保留 + 非法/截断负例）。
4. **writeUTF 编码长度边界**（65535 合法、65536/32768 NUL 抛、32767 NUL 合法、代理对 6 字节 CESU-8）。
5. **readFully 前缀保留 + len==0 no-op + 精确区域 + 越界/null**。
6. **skipBytes 负/零/部分**。
7. **readLine 行界矩阵**（\r、\r\n 合一、\n、EOF null、无终结符尾行）。
8. **close 后行为**（FIS 支撑：read/readInt/readLine/available → IOException "Stream Closed"；skipBytes 类型级）。
9. **委托与双层 DataInputStream**。
10. **static readUTF(RandomAccessFile) 跨实现**。
11. **JDK-27 特有（JDK-9+ 继承 API 经子类引用）**：readAllBytes 排空 + EOF、readNBytes(b,off,len) 区域与剩余、transferTo 只搬未读剩余并 EOF。

负例矩阵与消息子串：与 JDK-8 侧相同（见 JDK-8 同名文件表）；两处版本差异——close 后 skipBytes JDK-27 为 "Bad file descriptor"（JDK-8 "Stream Closed"）、writeUTF 超长消息 JDK-27 含字符串预览（共同子串 "too long: N bytes"）。javap-27 确认本类自身无新增公有方法，差异全部来自继承的 InputStream（readAllBytes/readNBytes/transferTo）。

产出：`DataInputStreamTest.java` 28 → 149 断言（jtreg Passed）。
