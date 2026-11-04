# java.io.BufferedInputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐缓冲语义（逐字节源验证缓冲）、构造器负容量 IAE、
> null 委托视为已关闭、read 参数校验、mark/reset 失效/未标记 IOException、close 后 IOException。

## 1. API 提供了哪些功能

- **构造器（2）**：`BufferedInputStream(InputStream)`、`BufferedInputStream(InputStream,int)`（size<=0 → IAE）。
- **读**：`read()`、`read(byte[],int,int)`。
- **定位/状态**：`skip(long)`（负数 0）、`available()`、`markSupported()`（true）、`mark(int)`、`reset()`。
- **生命周期**：`close()`（幂等，关闭后 read/available/reset → IOException）。

## 2. Java 用户如何使用

- 用大缓冲减少底层小读；`mark(readlimit)` 后 `reset()` 重放；越过 readlimit 后 mark 失效 reset 抛 IOException。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** + `src/share/classes/java/io/BufferedInputStream.java`。
- **实现源码**：`read(byte[],off,len)` 位运算校验（`(off|len|off+len|b.length-off-len)<0`→IOBE）、`skip(n<=0)→0`、`mark(int)` 不校验 readlimit（负值不抛）、`reset()` markpos<0 → IOException。

## 4. 测试用例设计（映射到 BufferedInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：默认可用、size<=0 IAE、null 委托读时 IOException | testConstructors |
| P0 | 缓冲：逐字节源验证缓冲分块正确 | testBuffering |
| P0 | read(byte[],off,len) 校验（NPE/IOBE）+ len==0 + 精确 offset | testReadArrayValidation |
| P1 | skip 边界（负数/越过末尾）；available | testSkip、testAvailable |
| P0 | mark/reset：重放、未标记 IOException、越过 readlimit 失效 IOException | testMarkReset |
| P1 | close 幂等 + 关闭后 IOException | testClose |

## 关键规范细节（易错，已锁入断言）

- `mark(int)` 不校验负 readlimit（与 BufferedReader 不同，BufferedReader.mark(-1) 才抛 IAE）。
- `skip(n<=0)` 返回 0；`reset()` 在 markpos<0（未标记/失效）抛 IOException。
- 构造器传 null 委托不立即抛，首次读视为「流已关闭」抛 IOException。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
- JDK-27 侧同源 + 反射白盒 + readAllBytes。

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（18 -> 66）。

### 新增维度
- 关闭态消息：close 后 read/read(byte[])/read(b,off,len)/available/skip/reset 全部 "Stream closed"（6 处，双侧实测同文）；mark after close 无害无操作。
- "Resetting to invalid mark"：无 mark 与跨 readlimit 重填后 reset 的精确消息。
- mark 超过 readlimit 仍有效：缓冲区持有数据时（默认 8192 缓冲 mark(2) 读 100；8 缓冲 mark(100) 读 30 跨重填）。
- fill 语义：向下层精确请求缓冲区大小（64 可测；默认 8192 经委托录制可观测）；read(byte[]) 一次填满缓冲再切。
- available 每次调用都咨询下层；available = 下层 + 已缓冲。
- skip × 缓冲交互（双侧实测关键语义）：缓冲区内有数据时 skip 只用缓冲字节（skip(10) 只有 7 个已缓冲返回 7，不向下层要差额）；空缓冲直接委托（100000 -> 100 全跳）。
- close 传播：一层/两层；跨块（64 缓冲 10KB）内容一致；奇数块 7 字节循环读。

### 负例精确异常表（双侧实测）
| 操作 | 类型 | 消息子串 |
|---|---|---|
| close 后 6 种操作 | IOException | "Stream closed"（双侧一致） |
| reset 无 mark / 越限 | IOException | "Resetting to invalid mark"（双侧一致） |
| 构造 size<=0 | IllegalArgumentException | "Buffer size <= 0"（双侧一致） |

### 版本差异
- transferTo 仅 JDK-9+；JDK-8 侧不引用。
