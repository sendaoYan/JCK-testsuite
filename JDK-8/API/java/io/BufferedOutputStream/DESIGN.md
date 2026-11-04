# java.io.BufferedOutputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。重点补齐构造器负容量 IAE、write(int) 低 8 位与缓冲、write 参数校验、
> 大块直写路径、flush/close 语义。

## 1. API 提供了哪些功能

- **构造器（2）**：`BufferedOutputStream(OutputStream)`、`BufferedOutputStream(OutputStream,int)`（size<=0 → IAE）。
- **写**：`write(int)`（低 8 位，缓冲）、`write(byte[],int,int)`（len>=buf.length 直写）、`flush()`。

## 2. Java 用户如何使用

- 写小数据先入缓冲，flush/close 或缓冲满时刷到底层；大块数据直写避免二次拷贝。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** + `src/share/classes/java/io/BufferedOutputStream.java`。
- **实现源码**：`write(byte[],off,len)` 无显式校验，null/坏索引由 `System.arraycopy` 抛 NPE/AIOOBE；`len>=buf.length` 走 `flushBuffer()+out.write` 直写。

## 4. 测试用例设计（映射到 BufferedOutputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器：size<=0 IAE | testConstructors |
| P0 | write(int) 低 8 位 + 缓冲（flush 前底层为空） | testWriteIntAndBuffering |
| P0 | write(byte[],off,len) 校验（NPE/IOBE） | testWriteArrayValidation |
| P1 | 大块写直写底层 + 内容一致 | testLargeWrite |
| P0 | flush 清缓冲；close 隐含 flush 剩余字节 | testFlushClose |

## 关键规范细节（易错，已锁入断言）

- `write(byte[],off,len)` 校验依赖底层，坏索引抛 IndexOutOfBoundsException（ArrayIndexOutOfBoundsException 子类）、null 抛 NPE。
- `len>=buf.length` 时直写底层，不经过缓冲。

## 注意事项

- 无墙钟/随机/默认 Locale 依赖；headless 安全。
- JDK-27 侧同源 + 反射白盒。
