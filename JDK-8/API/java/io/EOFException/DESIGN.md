# java.io.EOFException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；表示数据流意外结束。

## 2. Java 用户如何使用
- 读 DataInput/RandomAccessFile/ObjectInputStream 原始类型时以 IOException/EOFException 捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/EOFException.java`；`DataInputStream.readFully/readUTF` 等抛出。

## 4. 测试用例设计 + 自动化实现（EOFExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器默认 null / 消息保留。
2. readInt 仅 2 字节 → EOFException。
3. 空流上 readLong/readDouble/readChar/readUTF/readFully 各抛 EOFException；readLine/skipBytes 以 null/0 表示 EOF（对比）。
4. readFully 短流填充前缀后抛 EOFException。
5. RandomAccessFile readByte/readInt 越界抛 EOFException。
6. ObjectInputStream 载荷耗尽后 readInt 抛 EOFException。
7. 原始 InputStream.read 返回 -1（对比）。
- 健壮性: 确定性字节序列；无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 53，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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
