# java.io.BufferedOutputStream 测试设计案（JDK-27）

> 覆盖目标：**完整契约**（黑盒）+ 反射白盒结构。与 JDK-8 侧同源。

## 1. API 提供了哪些功能

- **构造器（2）**：`BufferedOutputStream(OutputStream)`、`BufferedOutputStream(OutputStream,int)`（size<=0 → IAE）。
- **写**：`write(int)`（低 8 位，缓冲）、`write(byte[],int,int)`（len>=buf.length 直写）、`flush()`。

## 2. Java 用户如何使用

- 写小数据先入缓冲，flush/close 或缓冲满时刷到底层；大块直写。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** + `~/git/jdk/src/java.base/share/classes/java/io/BufferedOutputStream.java`。

## 4. 测试用例设计（映射到 BufferedOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 反射白盒：继承 FilterOutputStream、write(int)/flush 存在 | testWhiteBox |
| P0 | 构造器 size<=0 IAE；write(int) 低 8 位 + 缓冲 | testConstructors、testWriteIntAndBuffering |
| P0 | write 校验（NPE/IOBE） | testWriteArrayValidation |
| P1 | 大块直写 | testLargeWrite |
| P0 | flush 清缓冲；close 隐含 flush | testFlushClose |

## 关键规范细节（易错，已锁入断言）

- `write(byte[],off,len)` 坏索引抛 IndexOutOfBoundsException、null 抛 NPE；`len>=buf.length` 直写。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
