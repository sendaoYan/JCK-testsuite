# java.io.DataOutputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐 write 参数校验（NPE/IOBE）、size() 计数、writeBytes/writeChars
> 字节布局、writeUTF 长度前缀与 >65535→UTFDataFormatException。

## 1. API 提供了哪些功能

- **构造器**：`DataOutputStream(OutputStream)`（不立即校验 null，写时 NPE）。
- **写**：`write(int)`（低 8 位）、`write(byte[],int,int)`、`flush()`。
- **原始类型写**：`writeBoolean/writeByte/writeShort/writeChar/writeInt/writeLong/writeFloat/writeDouble`。
- **文本写**：`writeBytes(String)`（每字符低 8 位）、`writeChars(String)`（每字符 2 字节大端）、`writeUTF(String)`。
- **计数**：`size()`（已写字节数）。

## 2. Java 用户如何使用

- 与 `DataInputStream` 成对做二进制序列化；`size()` 返回已写字节数；`writeUTF` 写入 2 字节长度前缀 + 修改 UTF-8。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** + `src/share/classes/java/io/DataOutputStream.java`。
- **实现源码**：`write(int)` 存低 8 位；`write(byte[],off,len)` 委托底层 `out.write`；`writeUTF` 编码长度 >65535 → `UTFDataFormatException`。

## 4. 测试用例设计（映射到 DataOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器 null：不立即抛、写时 NPE | testConstructorNull |
| P0 | write(int) 低 8 位 | testWriteInt |
| P0 | write(byte[],off,len) 校验（NPE/IOBE） | testWriteArrayValidation |
| P0 | 9 种原始类型与 DataInputStream 往返 | testPrimitivesRoundTrip |
| P0 | size() 各写类型宽度累加（含 writeUTF 2+len） | testSize |
| P1 | writeBytes 低 8 位、writeChars 大端 2 字节/字符 | testWriteBytesChars |
| P0 | writeUTF 长度前缀 + >65535→UTFDataFormatException | testWriteUTF |
| P1 | flush 委托到底层 | testFlush |

## 关键规范细节（易错，已锁入断言）

- `writeUTF` 的 2 字节长度是**编码后的字节数**（非字符数）；编码字节数 >65535 抛 UTFDataFormatException。
- 构造器不立即校验 null。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
- JDK-27 侧同源 + 反射白盒。
