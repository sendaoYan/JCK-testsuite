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
