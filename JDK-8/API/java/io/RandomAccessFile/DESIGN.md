# java.io.RandomAccessFile 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐模式 r/rw/rws/rwd 与非法模式拒绝、DataInput/DataOutput
> 往返（含 readUTF/writeUTF）、getFilePointer/seek/setLength/length/skipBytes、稀疏扩展、
> 负 seek、readLine、readFully、缺失文件 FileNotFoundException、关闭后 IOException。

## 1. API 提供了哪些功能

- **构造器（2）**：`(String,String)`、`(File,String)`；模式限定 `r`/`rw`/`rws`/`rwd`，非法模式抛 IAE。
- **随机访问**：`seek(long)`（负值抛 IOException）、`getFilePointer()`、`length()`、`setLength(long)`。
- **DataInput/DataOutput**：read/write 全部基本类型 + `readUTF`/`writeUTF`、`readLine`、`readFully`、`skipBytes`、`readUnsignedByte` 等。
- **生命周期**：`close()` 幂等；关闭后 read/seek 抛 IOException。

## 2. Java 用户如何使用

- 以 `rw` 打开后读写；用 `seek` 随机定位；用 `getFilePointer` 跟踪位置。
- 依赖 `setLength` 截断/扩展（扩展填零）；依赖 `seek` 越过末尾再写产生稀疏洞。
- 依赖 `readLine` 处理 CRLF/LF/无终止行；依赖 `readFully` 要么读满要么 EOFException。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/RandomAccessFile.html`）：模式集合、seek 负值抛 IOException、setLength 扩展填零、readLine 语义、readFully 语义。
- **JDK 实现源码**（`src/share/classes/java/io/RandomAccessFile.java`）：非法模式消息 "Illegal mode ..."、seek 负值消息 "Negative seek offset"。

## 4. 测试用例设计（映射到 RandomAccessFileTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | DataOutput 往返（boolean/byte/short/char/int/long/float/double/UTF/bytes） | testDataRoundTrip |
| P1 | r 模式只读（写抛 IOException）、getFD/getChannel | testReadOnlyMode |
| P1 | rws/rwd 模式可读写 | testSyncModes |
| P0 | 非法模式 IAE + 精确消息 | testIllegalModes |
| P1 | 缺失文件 FileNotFoundException | testMissingFile |
| P1 | readLine CRLF/LF/无终止/EOF null | testReadLine |
| P0 | setLength 截断/扩展填零/清零 | testSetLength |
| P1 | skipBytes 前进/EOF 钳制/负数 0 | testSkipBytes |
| P1 | seek 越过末尾写产生稀疏洞 | testSparseExtension |
| P1 | 负 seek IOException | testNegativeSeek |
| P1 | readFully 读满/越界 EOFException、零长度读 | testReadFully |
| P1 | close 后 read/seek IOException、close 幂等 | testClosed |

## 关键规范细节（易错，已锁入断言）

- 非法模式消息为 `Illegal mode "<mode>" must be one of "r", "rw", "rws", or "rwd"`。
- `setLength` 扩展部分读作 0；`seek` 越过末尾再写产生稀疏洞（中间读作 0）。
- `skipBytes(-n)` 返回 0；`seek(-n)` 抛 `IOException("Negative seek offset")`。
- `readFully` 在不足时抛 EOFException；`readLine` 在 EOF 返回 null。
- lambda 引用需 effectively final 的 `raf`，重赋值处改用局部 final 副本。

## 注意事项

- 全部操作在 `File.createTempFile` 隔离文件内，finally 关闭并删除；无墙钟/随机/Locale/Charset/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（实现 DataInput/DataOutput/Closeable、final getFD、方法枚举）。
