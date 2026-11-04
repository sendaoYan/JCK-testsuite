# java.io.RandomAccessFile 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(String,String)`/`(File,String)`，mode ∈ {r, rw, rws, rwd}（非法→IllegalArgumentException）。
- 定位：`getFilePointer()`、`seek(long)`（负→IOException("Negative seek offset")）、`length()`、`setLength(long)`（截断/零扩展）、`skipBytes(int)`（返回实际、负→0）。
- DataInput：readBoolean/Byte/UnsignedByte/Short/Char/Int/Long/Float/Double/readLine/readUTF/readFully/read（EOF→EOFException）。
- DataOutput：对应 write 系列 + writeBytes/writeChars/writeUTF。
- `getFD()`、`getChannel()`、`close()`（后继操作 "Stream Closed"）。

## 2. Java 用户如何使用
- 定长记录文件、索引文件：seek 到偏移读写；setLength 截断或预留稀疏空间；r 模式只读保护。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- mode 校验 — `jdk25u-dev/src/java.base/share/classes/java/io/RandomAccessFile.java:244`（Illegal mode）。
- 负 seek — 同文件 :673（Negative seek offset）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/File/`、`jdk25u-dev/test/jdk/java/io/DataInputStream/` 邻接用例。

## 4. 测试用例设计 + 自动化实现（RandomAccessFileTest.testBlackBox，探针 P10 先行）
1. 全类型 DataOutput 写入→seek(0)→全类型 DataInput 读回（含 writeByte(200)↔readUnsignedByte==200）。
2. r 模式写抛 "Bad file descriptor"；FD valid、channel 非 null。
3. rws/rwd 可用且写入可见；非法 mode（x/""/rwxd/R/rw+）精确消息。
4. 缺失文件 r→FileNotFoundException；readLine 剥 \r\n、末行无终止仍返回、EOF 后 null。
5. setLength 截断后 readInt 抛 EOF；扩展零填充；readFully 不足抛 EOF；len 0 读返回 0。
6. skipBytes 夹紧返回剩余、负数 0；seek(50)+write 稀疏扩展 length=51 且洞读 0。
7. seek(-1)→"Negative seek offset"；close 后 read/seek 抛 "Stream Closed"；close 幂等。
- 健壮性: createTempFile + finally close/delete；无全局文件副作用。

产出：`RandomAccessFileTest.java`（白盒 + 黑盒共 82 断言）。

## 深化追加（契约级）

> 双侧探针先行（JDK-8 / JDK-27 实测），新增 testCtorNullsAndEquivalence 等 14 个方法，
> 断言 52 → 169。除特别标注外消息为双侧相同子串。

### 新增维度清单（与 JDK-8 侧同构）
1. 构造 null 矩阵（name/File/mode null → NPE ×4）+ (String,String)≡(File,String)。
2. 近失模式 "rw "/"rwS"/"RWS"/"r ws"/"rwsx"/"rws " → IAE 精确消息。
3. 缺失/目录：r 缺失 FNFE 含路径；rw/rws 缺失创建；r/rw 目录 FNFE "(Is a directory)"。
4. r 模式：write "Bad file descriptor"、setLength "Invalid argument"。
5. close 后 read/write/seek/getFilePointer/length/readUTF 均 "Stream Closed"；FD invalid；close 幂等。
6. seek(-1)/MIN "Negative seek offset"；seek 超长 read -1 指针不动。
7. setLength(-1) → IOException "Invalid argument"（非 IAE，双侧实测）；截断钳指针；扩展零填充。
8. 线格式 BIG-endian 原始字节断言（boolean/short/int/long/char/UTF/chars/bytes）。
9. modified-UTF-8：""、NUL 2 字节、代理对、70000 字节 UTFDataFormatException、损坏字节 UTFDataFormatException。
10. EOF 类型化读 → EOFException。
11. 索引边界 → IndexOutOfBoundsException（普通型）；off==len len==0 合法。
12. 通道互操作：position 同步、channel.read 推进 getFilePointer。
13. readLine 孤立 '\r' 行界。
14. 碎片读 offset 区间、readUnsignedShort 不符号扩展。

### 负例精确异常表（双侧实测）
（同 JDK-8 侧 DESIGN 的完整表。）

### JDK-8 vs JDK-27 版本差异
- **getChannel() 在 close 之后首次创建**：JDK-27 isOpen()==false（JDK-8 为 true，实测 quirk）；先建后 close 双侧均 false。测试按版本各自断言。
- writeUTF 过长消息：27 多出字符串预览前缀（断言公共子串 "too long: 70000 bytes"）。
- mode null NPE：27 带 helpful 提示。
