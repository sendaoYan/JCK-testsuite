# java.io.Reader 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。针对抽象基类的**默认实现**：read()/read(char[]) 从
> read(char[],int,int) 派生、read(CharBuffer)、skip 负数→IAE、ready/mark/reset 默认值、
> read(char[]) 对 null 数组 NPE。

## 1. API 提供了哪些功能

- **抽象**：`read(char[],int,int)`、`close()`。
- **默认实现**：`read()`（单字符）、`read(char[])`（→ `read(cbuf,0,cbuf.length)`）、`read(CharBuffer)`。
- **定位/状态**：`skip(long)`（`n<0` 抛 IAE）、`ready()`（默认 false）、`mark(int)`（默认抛 IOException）、`reset()`（默认抛 IOException）、`markSupported()`（默认 false）。
- **接口**：`Readable`（提供 `read(CharBuffer)`）、`Closeable`。

## 2. Java 用户如何使用

- 继承 Reader 实现 `read(char[],int,int)` 与 `close()` 即可得到功能完整的字符输入流。
- 依赖 `read(char[])` 对 null 抛 NPE（`cbuf.length` 先求值）；依赖 `skip` 负数抛 IAE。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/Reader.html`）：默认实现契约、skip 负数抛 IAE、mark/reset 抛 IOException。
- **JDK 实现源码**（`src/share/classes/java/io/Reader.java`）：`skip(n<0)` 抛 "skip value is negative"、`mark` 抛 "mark() not supported"、`reset` 抛 "reset() not supported"、`read(char[])` 直接 `cbuf.length` 求值。

## 4. 测试用例设计（映射到 ReaderTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认 read()/read(char[]) 从 read(char[],int,int) 派生、EOF、零长度读 | testReadDefaults |
| P1 | read(char[]) 对 null 数组 NPE（cbuf.length 先求值） | testNullArray |
| P1 | read(CharBuffer) 内容与 EOF | testReadCharBuffer |
| P0 | skip 负数→IAE、0 返回 0、位置不动 | testSkip |
| P1 | markSupported false、ready false、mark/reset→IOException | testDefaults |
| P1 | 多态使用：具体 JDK 流经基类引用 | testPolymorphicUse |

## 关键规范细节（易错，已锁入断言）

- `read(char[],int,int)` 为**抽象**方法，基类不做 null/off/len 校验（由具体子类负责）；因此不写 `read(char[], off, -1)→IOBE` 之类的基类断言。
- `read(char[])` 是具体默认方法，`cbuf.length` 在进入子类前求值，故 `read((char[])null)` 抛 NPE。
- `skip(long)` 对负数抛 `IllegalArgumentException("skip value is negative")`。

## 注意事项

- 全部输入为固定字符数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒 + 黑盒 readAllLines/readAllAsString/transferTo/Reader.of/nullReader（JDK 9+/11+ 方法）。
