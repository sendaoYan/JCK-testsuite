# java.io.DataOutputStream 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 提供了哪些功能

- **构造器**：`DataOutputStream(OutputStream)`（不立即校验 null，写时 NPE）。
- **写**：`write(int)`（低 8 位）、`write(byte[],int,int)`、`flush()`。
- **原始类型写**：`writeBoolean/Byte/Short/Char/Int/Long/Float/Double`。
- **文本写**：`writeBytes(String)`、`writeChars(String)`、`writeUTF(String)`。
- **计数**：`size()`。

## 2. Java 用户如何使用

- 与 `DataInputStream` 成对做二进制序列化；`size()` 返回已写字节数；`writeUTF` 写 2 字节长度前缀 + 修改 UTF-8。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** + `~/git/jdk/src/java.base/share/classes/java/io/DataOutputStream.java`。

## 4. 测试用例设计（映射到 DataOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 反射白盒：继承 FilterOutputStream、实现 DataOutput、writeInt/writeLong/writeUTF/size 存在 | testWhiteBox |
| P0 | 构造器 null 写时 NPE；write(int) 低 8 位；write 校验（NPE/IOBE） | testConstructorNull、testWriteInt、testWriteArrayValidation |
| P0 | 9 原始类型往返；size() 计数 | testPrimitivesRoundTrip、testSize |
| P1 | writeBytes/writeChars 布局 | testWriteBytesChars |
| P0 | writeUTF 长度前缀 + >65535 UTFDataFormatException + null NPE | testWriteUTF |
| P1 | flush 委托 | testFlush |

## 关键规范细节（易错，已锁入断言）

- `writeUTF` 长度是编码字节数（非字符数）；>65535 → UTFDataFormatException；null → NPE。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
