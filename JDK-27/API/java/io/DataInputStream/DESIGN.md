# java.io.DataInputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `DataInputStream(InputStream)`
- 基础读: `read()`、`read(byte[])`、`read(byte[],int,int)`、`readFully(byte[])`、`readFully(byte[],int,int)`、`readNBytes(int)`
- 基本类型: `readBoolean`、`readByte`、`readUnsignedByte`、`readShort`、`readUnsignedShort`、`readChar`、`readInt`、`readLong`、`readFloat`、`readDouble`
- 定位: `skipBytes(int)`
- 字符串: `readUTF()`、`readUTF(DataInput)`
- 状态: `available()`
- 契约: `implements DataInput`（readLine 已废弃）

## 2. Java 用户如何使用
- `DataOutputStream` 写多类型 → `DataInputStream` 按同序读回：readInt/readUTF/readBoolean/readDouble 等。
- 组合链路：写 5 字节 → `readFully` 全量读；`skipBytes` 跳过字节；`readUTF` 读修改 UTF-8 字符串。

## 3. 开源代码实际用法调研（真实片段）
- `try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(flow))) {` — `jdk25u-dev/test/jdk/tools/jlink/plugins/CompressIndexesTest.java:86`
- `DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));` — `jdk25u-dev/src/java.base/share/classes/java/time/zone/TzdbZoneRulesProvider.java:134`
- `DataOutputStream dos = new DataOutputStream(baos);`（写侧配对）— `jdk25u-dev/test/jdk/java/io/DataInputStream/ReadUTF.java:65`

## 4. 测试用例设计 + 自动化实现（指向 DataInputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: 用 DataOutputStream 写 byte/short/int/long/float/double/boolean/char/UTF → DataInputStream 按序读回全等；readFully 全量；skipBytes 精确跳过；readNBytes。
- 边界: readUnsignedByte(0xFF)==255；readUnsignedShort 边界；EOF 读抛 `EOFException`。
- 健壮性: 固定大端字节序（DataInput 规范）；内存流输入，无全局副作用。
- 白盒 `testWhiteBox`: 继承 FilterInputStream、实现 DataInput、readUTF/readFully/skipBytes 存在。
