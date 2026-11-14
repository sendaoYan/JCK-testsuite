# java.io.RandomAccessFile 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐模式 r/rw/rws/rwd 与非法模式拒绝、DataInput/DataOutput
> 往返（含 readUTF/writeUTF）、getFilePointer/seek/setLength/length/skipBytes、稀疏扩展、
> 负 seek、readLine、readFully、缺失文件 FileNotFoundException、关闭后 IOException。

## 1. API 提供了哪些功能

- **构造器（2）**：`(String,String)`、`(File,String)`；模式限定 `r`/`rw`/`rws`/`rwd`，非法模式抛 IAE。
- **随机访问**：`seek(long)`（负值抛 IOException）、`getFilePointer()`、`length()`、`setLength(long)`。
- **DataInput/DataOutput**：read/write 全部基本类型 + `readUTF`/`writeUTF`、`readLine`、`readFully`、`skipBytes`、`readUnsignedByte` 等。
- **生命周期**：`close()` 幂等；关闭后 read/seek 抛 IOException。

## 2. Java 用户如何使用

- 以 `rw` 打开后读写；用 `seek` 随机定位；用 `getFilePointer` 跟踪位置。
- 依赖 `setLength` 截断/扩展（扩展填零）；依赖 `seek` 越过末尾再写产生稀疏洞。
- 依赖 `readLine` 处理 CRLF/LF/无终止行；依赖 `readFully` 要么读满要么 EOFException。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/RandomAccessFile.html`）：模式集合、seek 负值抛 IOException、setLength 扩展填零、readLine 语义、readFully 语义。
- **JDK 实现源码**（`src/share/classes/java/io/RandomAccessFile.java`）：非法模式消息 "Illegal mode ..."、seek 负值消息 "Negative seek offset"。

## 4. 测试用例设计（映射到 RandomAccessFileTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | DataOutput 往返（boolean/byte/short/char/int/long/float/double/UTF/bytes） | testDataRoundTrip |
| P1 | r 模式只读（写抛 IOException）、getFD/getChannel | testReadOnlyMode |
| P1 | rws/rwd 模式可读写 | testSyncModes |
| P0 | 非法模式 IAE + 精确消息 | testIllegalModes |
| P1 | 缺失文件 FileNotFoundException | testMissingFile |
| P1 | readLine CRLF/LF/无终止/EOF null | testReadLine |
| P0 | setLength 截断/扩展填零/清零 | testSetLength |
| P1 | skipBytes 前进/EOF 钳制/负数 0 | testSkipBytes |
| P1 | seek 越过末尾写产生稀疏洞 | testSparseExtension |
| P1 | 负 seek IOException | testNegativeSeek |
| P1 | readFully 读满/越界 EOFException、零长度读 | testReadFully |
| P1 | close 后 read/seek IOException、close 幂等 | testClosed |

## 关键规范细节（易错，已锁入断言）

- 非法模式消息为 `Illegal mode "<mode>" must be one of "r", "rw", "rws", or "rwd"`。
- `setLength` 扩展部分读作 0；`seek` 越过末尾再写产生稀疏洞（中间读作 0）。
- `skipBytes(-n)` 返回 0；`seek(-n)` 抛 `IOException("Negative seek offset")`。
- `readFully` 在不足时抛 EOFException；`readLine` 在 EOF 返回 null。
- lambda 引用需 effectively final 的 `raf`，重赋值处改用局部 final 副本。

## 注意事项

- 全部操作在 `File.createTempFile` 隔离文件内，finally 关闭并删除；无墙钟/随机/Locale/Charset/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（实现 DataInput/DataOutput/Closeable、final getFD、方法枚举）。

## 深化追加（契约级）

> 双侧探针先行（JDK-8 / JDK-27 实测），新增 testCtorNullsAndEquivalence 等 14 个方法，
> 断言 37 → 132。除特别标注外消息为双侧相同子串。

### 新增维度清单
1. **构造 null 矩阵**：name null NPE、File null NPE、mode null NPE（两个构造器各测）；(String,String) 与 (File,String) 等价（length/pointer/首字节）。
2. **近失模式**："rw "、"rwS"、"RWS"、"r ws"、"rwsx"、"rws " 全部 IAE，精确消息 `Illegal mode "<mode>" must be one of "r", "rw", "rws", or "rwd"`（大小写敏感，无 trim）。
3. **缺失/目录**：r 缺失 → FNFE（消息含完整路径 + 平台 OS 原因文本）；rw/rws 缺失 → 创建空文件；r/rw 打开目录 → FNFE 平台 OS 原因文本（随 OS/locale 本地化）。
4. **r 模式失败消息**：write/writeInt → IOException "Bad file descriptor"；setLength → IOException "Invalid argument"；FD valid、channel isOpen（打开时）。
5. **close 后统一 "Stream Closed"**：read/write/seek/getFilePointer/length/readUTF 六操作；getFD() 仍可用且 valid()==false；close 幂等。
6. **seek 边界**：-1 与 Long.MIN_VALUE → IOException "Negative seek offset"；seek 超长合法、read → -1 且指针不动。
7. **setLength 边界**：**setLength(-1) 双侧均为 IOException "Invalid argument"（注意：不是 javadoc 早期版本暗示的 IAE）**；截断后指针钳到新长度（seek(8)+setLength(4)→pointer 4）；扩展区读 0。
8. **线格式（BIG-endian，原始字节断言）**：boolean true→01；short 0xBEEF→BE EF；int→4 字节大端；long→8 字节大端；char 'Z'→00 5A；writeUTF("A")→00 01 41（2 字节大端长度前缀）；writeChars("AB")→00 41 00 42；writeBytes 保留低 8 位（'é'→0xE9）。
9. **modified-UTF-8 边界**："" 往返；"\u0000" 编码 2 字节（00 02）且往返；代理对往返；70000 字节 → UTFDataFormatException "too long: 70000 bytes"；损坏字节（0xFF）→ UTFDataFormatException "malformed input around byte 0"。
10. **EOF 类型化读**：readBoolean/readInt/readUTF/readFully 在 EOF → EOFException。
11. **索引边界**：read/write/readFully 的 (b,-1,0)/(b,0,-1)/(b,3,2) → IndexOutOfBoundsException（普通型，非 Array 子类）；(b,4,0) off==len 且 len==0 合法返回 0/无操作。
12. **通道互操作**：seek 后 getChannel().position() 同步；channel.read 推进 getFilePointer。
13. **readLine**：孤立 '\r' 亦为行界（"a\rb\rc"→a/b/c/null）。
14. **碎片读**：read 尾部返回剩余数；read(b,off,len)/readFully(b,off,len) 只动 off..off+len 区间；readUnsignedShort(0xBEEF)==48879 不符号扩展。

### 负例精确异常表（双侧实测）
| 操作 | 异常 | 消息（子串） |
|---|---|---|
| ctor name/File/mode null | NullPointerException | —（类型级） |
| mode "rw "/"rwS"/"RWS"/"r ws"/"rwsx"/"rws " | IllegalArgumentException | `Illegal mode "<m>" must be one of "r", "rw", "rws", or "rwd"` |
| "r" 打开缺失文件 | FileNotFoundException | 含路径 + 平台 OS 原因文本 |
| "r"/"rw" 打开目录 | FileNotFoundException | 平台 OS 原因文本 |
| r 模式 write/writeInt | IOException | "Bad file descriptor" |
| r 模式 setLength | IOException | "Invalid argument" |
| seek(-1)/seek(Long.MIN_VALUE) | IOException | "Negative seek offset" |
| setLength(-1) | IOException | "Invalid argument"（**非 IAE**） |
| close 后 6 种操作 | IOException | "Stream Closed" |
| read/write/readFully 越界索引 | IndexOutOfBoundsException | —（普通型） |
| EOF 处 readBoolean/readInt/readUTF/readFully | EOFException | — |
| writeUTF 70000 字节 | UTFDataFormatException | "too long: 70000 bytes" |
| readUTF 损坏字节 | UTFDataFormatException | "malformed input around byte 0" |

### JDK-8 vs JDK-27 版本差异
- **getChannel() 在 close 之后首次创建**：JDK-8 返回的 channel isOpen()==true（懒建通道对象未经历 close，实测 quirk）；JDK-27 为 false（已修复）。测试双侧各自断言并在用例注释记录。先建后 close 的通道双侧均 isOpen()==false。
- writeUTF 过长消息：8=`encoded string too long: 70000 bytes`，27=`encoded string (aaa…) too long: 70000 bytes`（断言公共子串 "too long: 70000 bytes"）。
- mode null NPE 消息：27 带 helpful 提示，8 为 null（类型级断言）。
