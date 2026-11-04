# java.io.PushbackReader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐构造器参数校验、LIFO 回退、缓冲满 IOException、
> read(char[],off,len) 参数校验、ready、skip 语义（负数抛 IAE）、mark/reset 不支持、
> close 后 IOException。

## 1. API 提供了哪些功能

- **构造器（2）**：`(Reader)`（回退缓冲 1 字符）、`(Reader,int size)`（`size<=0` 抛 IAE）。
- **回退**：`unread(int)`、`unread(char[])`、`unread(char[],off,len)`；缓冲满抛 `IOException("Pushback buffer overflow")`。
- **读**：`read()` / `read(char[],int,int)`（先消费回退区）。
- **定位/状态**：`ready()`（回退区有数据或底层 ready）、`skip(long)`（`n<0` 抛 IAE，先消费回退区）、`markSupported()==false`、`mark(int)` 与 `reset()` 抛 IOException。
- **生命周期**：`close()` 幂等；关闭后 read/unread 抛 `IOException("Stream closed")`。

## 2. Java 用户如何使用

- 字符级词法扫描：读一字符判定类型，不合则 `unread` 回去重新解析。
- 依赖 LIFO 回退顺序与默认 1 字符缓冲；依赖 `skip` 先耗尽回退区。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/PushbackReader.html`）：构造器 size<=0 抛 IAE、unread 满抛 IOException、mark/reset 不支持、skip 负数抛 IAE。
- **JDK 实现源码**（`src/share/classes/java/io/PushbackReader.java`）：`unread(char[],off,len)` 依赖 `System.arraycopy` 做 null/越界校验、`skip(n<0)` 显式抛 "skip value is negative"。

## 4. 测试用例设计（映射到 PushbackReaderTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：默认缓冲 1、size<=0 抛 IAE | testConstructors |
| P0 | unread(int) 先出、LIFO 多次回退、EOF | testUnreadInt |
| P0 | unread(char[])/unread(char[],off,len) 区域回退、精确内容 | testUnreadArray |
| P0 | 缓冲满抛 IOException、失败后缓冲原样、null/越界（新鲜非满缓冲） | testBufferOverflow |
| P0 | read(char[],off,len) 参数校验：null→NPE、off/len 非法→IOBE、len==0→0 | testReadArrayValidation |
| P1 | ready 语义；skip 先消费回退区、负数抛 IAE、0 返回 0 | testReadySkip |
| P1 | markSupported false、mark/reset→IOException | testMarkReset |
| P1 | close 幂等、关闭后 read/unread→IOException | testClose |

## 关键规范细节（易错，已锁入断言）

- `unread(char[],off,len)` **不显式校验** null/off/len，只在 `System.arraycopy` 时抛 NPE/IOBE；缓冲满时先抛 IOException。断言 null/越界须用**未满**的新实例。
- `skip(long)` 对负数抛 `IllegalArgumentException("skip value is negative")`（区别于 `PushbackInputStream.skip` 对 `n<=0` 返回 0）。
- 默认构造器的回退缓冲大小恰为 1，第二次 `unread` 即抛 IOException。
- `mark(int)` 与 `reset()` 均抛 `IOException("mark/reset not supported")`。

## 注意事项

- 全部输入为固定字符串，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（继承 FilterReader、构造器、方法枚举）。
