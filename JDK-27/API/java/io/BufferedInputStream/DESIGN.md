# java.io.BufferedInputStream 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源，另覆盖 `readAllBytes`。

## 1. API 提供了哪些功能

- **构造器（2）**：`BufferedInputStream(InputStream)`、`BufferedInputStream(InputStream,int)`（size<=0 → IAE）。
- **读**：`read()`、`read(byte[],int,int)`、继承 `readAllBytes()`。
- **定位/状态**：`skip(long)`、`available()`、`markSupported()`、`mark(int)`、`reset()`。
- **生命周期**：`close()`（幂等，关闭后 read/available/reset → IOException）。

## 2. Java 用户如何使用

- 用大缓冲减少底层小读；`mark(readlimit)` 后 `reset()` 重放；越过 readlimit 后 reset 抛 IOException。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** + `~/git/jdk/src/java.base/share/classes/java/io/BufferedInputStream.java`。

## 4. 测试用例设计（映射到 BufferedInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 反射白盒：继承 FilterInputStream、read/mark/reset/markSupported 存在 | testWhiteBox |
| P0 | 构造器：size<=0 IAE、null 委托读时 IOException | testConstructors |
| P0 | 缓冲分块正确 + readAllBytes | testBuffering |
| P0 | read 校验（NPE/IOBE）+ len==0 + 精确 offset | testReadArrayValidation |
| P1 | skip 边界；available | testSkip、testAvailable |
| P0 | mark/reset：重放、未标记 IOException、越过 readlimit 失效 | testMarkReset |
| P1 | close 幂等 + 关闭后 IOException | testClose |

## 关键规范细节（易错，已锁入断言）

- `mark(int)` 不校验负 readlimit；`reset()` markpos<0 抛 IOException；null 委托首次读视为关闭。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
