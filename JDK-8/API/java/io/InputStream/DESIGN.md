# java.io.InputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。针对抽象基类的**默认实现**：read(byte[]) 与
> read(byte[],off,len) 从 read() 派生、skip 负数→0、available/close/mark/reset 默认值、
> 参数校验、多态消费。

## 1. API 提供了哪些功能

- **抽象**：`read()`（唯一抽象方法）；子类仅实现 read() 即获得其余读语义。
- **默认实现**：`read(byte[])` → `read(b,0,b.length)`；`read(byte[],int,int)` 逐字节循环 read()。
- **定位/状态**：`skip(long)`（`n<=0` 返回 0）、`available()`（默认 0）、`mark(int)`（默认 no-op）、`reset()`（默认抛 IOException）、`markSupported()`（默认 false）。
- **生命周期**：`close()` 默认 no-op（不关闭底层）。

## 2. Java 用户如何使用

- 继承 InputStream 只实现 `read()` 即可得到一个功能完整的字节输入流。
- 依赖零长度读返回 0、EOF 返回 -1、`skip` 负数/0 不移动位置。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/InputStream.html`）：各默认实现契约、skip 负数返回 0、reset 抛 IOException。
- **JDK 实现源码**（`src/share/classes/java/io/InputStream.java`）：`skip(n<=0)` 返回 0、`reset` 抛 "mark/reset not supported"、`read(byte[],off,len)` 逐字节循环。

## 4. 测试用例设计（映射到 InputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认 bulk read 从 read() 派生、EOF -1、零长度读 0 | testBulkReadDefaults |
| P0 | 参数校验：null→NPE、off/len 非法→IOBE | testArgumentChecking |
| P1 | skip 负数/0 返回 0、越界返回剩余 | testSkip |
| P1 | available 默认 0、close 默认 no-op、markSupported false、mark no-op、reset 抛 IOException | testDefaults |
| P1 | 多态使用：具体 JDK 流经基类引用 | testPolymorphicUse |

## 关键规范细节（易错，已锁入断言）

- `skip(long)` 对 `n<=0` 返回 0 且不移动位置；对超过剩余字节数返回实际剩余。
- `close()` 为 no-op，关闭后 read 仍可返回数据（区别于带资源的子类）。
- `reset()` 抛 `IOException("mark/reset not supported")`；`mark(int)` 为 no-op 不抛。
- `read(byte[],off,len)` 在 `len==0` 时返回 0；在 EOF 时返回 -1（即使 len>0）。

## 注意事项

- 全部输入为固定字节数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（抽象类、Closeable、read 抽象）+ 黑盒 readAllBytes/readNBytes/skipNBytes/transferTo/nullInputStream（JDK 9+/11+ 方法）。
