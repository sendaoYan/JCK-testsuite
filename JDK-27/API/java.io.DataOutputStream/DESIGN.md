# java.io.DataOutputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `DataOutputStream(OutputStream)`
- 基础写: `write(int)`、`write(byte[],int,int)`
- 基本类型: `writeBoolean`、`writeByte`、`writeShort`、`writeChar`、`writeInt`、`writeLong`、`writeFloat`、`writeDouble`、`writeBytes(String)`
- 字符串: `writeUTF(String)`、`writeChars(String)`
- 刷新/状态: `flush()`、`size()`、`close()`
- 契约: `implements DataOutput`

## 2. Java 用户如何使用
- `new DataOutputStream(baos)` 顺序写多类型，`size()` 返回已写字节数，`flush` 刷到目标。
- 组合链路：写 byte/short/int/long/float/double/boolean/char → `size()` → `DataInputStream` 读回全等。

## 3. 开源代码实际用法调研（真实片段）
- `return new DataOutputStream(new ByteArrayOutputStream());` — `jdk25u-dev/test/jdk/tools/jlink/ImageFileCreatorTest.java:211`
- `DataOutputStream dos = new DataOutputStream(baos);` — `jdk25u-dev/test/jdk/java/io/DataInputStream/ReadUTF.java:65`
- `uo = new DataOutputStream(us);` — `jdk25u-dev/test/jdk/java/io/charStreams/LineLengthsSource.java:39`

## 4. 测试用例设计 + 自动化实现（指向 DataOutputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: 写 byte/short/int/long/float/double/boolean/char → `size()` 精确字节数（1/2/4/8/4/8/1/2）→ 读回全等；writeUTF 修改 UTF-8 长度前缀；writeBytes/writeChars。
- 边界: `size()` 累计准确；flush 后目标流可见。
- 健壮性: 固定大端字节序；内存流目标；无全局副作用。
- 白盒 `testWhiteBox`: 继承 FilterOutputStream、实现 DataOutput、writeUTF/size 存在。
