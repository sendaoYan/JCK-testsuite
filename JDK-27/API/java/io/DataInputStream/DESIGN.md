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
