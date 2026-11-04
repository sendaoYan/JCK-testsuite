# java.io.DataInputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐每个 readXxx 在流末尾的 EOFException、readFully 的
> 精确填充/短读 EOF/len<0 IOBE/null NPE、skipBytes 实际跳过数、readUTF 修改 UTF-8 与
> null 往返、static readUTF、readLine、read(byte[],off,len) 校验。

## 1. API 提供了哪些功能

- **构造器**：`DataInputStream(InputStream)`（不立即校验 null，读时 NPE）。
- **原始类型读**：`readBoolean/readByte/readUnsignedByte/readShort/readUnsignedShort/readChar/readInt/readLong/readFloat/readDouble`。
- **批量读**：`readFully(byte[])`、`readFully(byte[],int,int)`、`read(byte[])`、`read(byte[],int,int)`、`skipBytes(int)`。
- **文本**：`readUTF()`（修改 UTF-8）、`static readUTF(DataInput)`、`readLine()`（弃用）。

## 2. Java 用户如何使用

- 与 `DataOutputStream` 成对做二进制往返；读越界时依赖 `EOFException` 判定数据截断。
- 依赖 `readUnsignedByte/Short` 读取 0..255/0..65535；`readFully` 一次性精确取满。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** + `src/share/classes/java/io/DataInputStream.java`。
- **实现源码**：`readFully(byte[],off,len)` 仅校验 `len<0→IOBE`，循环 `in.read` 返回负 → `EOFException`；`skipBytes` 返回实际跳过数（可能小于 n）。

## 4. 测试用例设计（映射到 DataInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器 null：不立即抛、读时 NPE | testConstructorNull |
| P0 | 9 种原始类型与 DataOutputStream 往返 | testReadPrimitivesRoundTrip |
| P0 | readUnsignedByte/Short 0..255/0..65535 | testUnsignedReads |
| P0 | 每种 readXxx 空流→EOFException；截断→EOFException | testEOFExceptions |
| P0 | readFully：精确填充、短读 EOF、len<0 IOBE、null NPE | testReadFully |
| P1 | skipBytes 实际跳过数/越过末尾/EOF 返回 0 | testSkipBytes |
| P0 | readUTF 空/ASCII/非 ASCII/null 往返 + static readUTF | testReadUTF |
| P1 | readLine 处理 \n、\r\n、EOF | testReadLine |
| P1 | read(byte[],off,len) 校验（NPE/IOBE） | testReadArrayValidation |

## 关键规范细节（易错，已锁入断言）

- 构造器不立即校验 null；`new DataInputStream(null)` 合法，首次 read 才 NPE。
- `readFully(byte[],off,len)` 只校验 `len<0`；null/负 off 由底层 `in.read` 抛 NPE/IOBE。
- `skipBytes(n)` 返回实际跳过数（读到 EOF 可能小于 n）。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；文本用显式 US-ASCII；headless 安全。
- JDK-27 侧同源 + 反射白盒。
