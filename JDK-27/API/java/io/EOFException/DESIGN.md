# java.io.EOFException 测试方案 (DESIGN)

## 1. API 功能
- `EOFException()` / `(String)`：读入过程中意外到达流末尾或读到数据中段时抛出。
- DataInput 的多字节原语读（readInt/readLong/readChar/readUTF/readFully）与 RandomAccessFile 的同类读都会抛；`InputStream.read()` 用 -1 而非异常表示 EOF。

## 2. Java 用户如何使用
- 读取定长二进制记录/协议帧时区分"数据不完整"（EOFException）与"流正常结束"（read()==-1）。

## 3. 开源代码实际用法调研
- DataInputStream 多字节读在读尽可用字节后抛 EOF — `jdk25u-dev/src/java.base/share/classes/java/io/DataInputStream.java`（readInt/readFully 委托）。
- RandomAccessFile 原语读在 EOF 抛出 — `jdk25u-dev/src/java.base/share/classes/java/io/RandomAccessFile.java:503`（readFully）。
- readLine/skipBytes 以 null/0 表示 EOF（不抛）— 同文件与 DataInputStream 源码。

## 4. 测试用例设计 + 自动化实现（EOFTest.java→EOFExceptionTest.java）
- 白盒: extends IOException、两构造。
- 黑盒场景:
  1. 构造器 message。
  2. 2 字节流上 readInt 抛出。
  3. 空流上 readLong/readDouble/readChar/readUTF/readFully 逐一抛出；readLine/skipBytes 返回 null/0 不抛（对照）。
  4. readFully 半填缓冲后抛出（可用字节已写入、剩余未触碰）。
  5. RandomAccessFile: writeShort+seek(0)+readShort 正常；文件尾 readInt/readByte 抛出、readLine 返回 null；seek(0) 后仅 2 字节时 readInt 抛出。
  6. ObjectInputStream 载荷耗尽后 readInt 抛出。
  7. 空文件 FileInputStream.read / bulk read 返回 -1（对照）。
- 健壮性: 临时文件 finally 删除；读写同 try-with-resources。

产出：`EOFExceptionTest.java`（白盒 3 + 黑盒 21 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 57，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `EOFException`：双侧同值 `6433858223774886977L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract（suid + 往返）
3. testTriggeredSemantics（readByte/readBoolean/readUnsignedByte/readUnsignedShort/readFloat/截断 readUTF/零长 readFully 成功对照/Reader.read() 返回 -1 对照）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- readByte 截断 → EOFException（无 message）
- readUTF 截断 → EOFException（与 UTFDataFormatException 区分）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
