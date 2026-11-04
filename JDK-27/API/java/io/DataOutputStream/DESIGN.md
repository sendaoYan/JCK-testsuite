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

## 深化追加（契约级，批次 ioC）

新增维度清单（与 JDK-8 侧完全镜像 + JDK-27 特有追加 testNullOutputStreamSink）：

1. **逐 writeXxx 边界字节精确**（82 字节镜像：MIN/MAX/'\u0000'/'￿'/0.0/-0.0/NaN/±Inf 逐字节点检）。
2. **writeXxx(int) 低位置截断**（34 / 56 78）。
3. **size() 全操作矩阵 + flush/close 不清零 + 写穿透**。
4. **IEEE 754 位直通**（float/double 位型逐字节 + 往返）。
5. **writeUTF 编码长度边界**（65535 ASCII / 21845 中文合法；65536 / 21846 中文 / 32768 NUL 抛 UTFDataFormatException）。
6. **flush 传播探针计数** + close 幂等。
7. **文件 sink close 后写** IOException "Stream Closed" 且文件不变长。
8. **writeUTF 与 ObjectOutputStream 块数据互操作**（AC ED 00 05 77 05 + writeUTF 帧）。
9. **JDK-27 特有**：`OutputStream.nullOutputStream()`（JDK-11+ 静态工厂）作 sink——计数器照常累加、flush/close 无异常。

负例矩阵与消息子串：与 JDK-8 侧相同；版本差异同 JDK-8 记录（writeUTF 超长消息预览、二次 close 的 sink 传播次数 2 vs 1）。

产出：`DataOutputStreamTest.java` 22 → 94 断言（jtreg Passed）。
