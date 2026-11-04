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

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（11 -> 61）：JDK-8 侧全部镜像维度（自动刷新边界、数组路径矩阵含 partial-fit、默认 8192、flush 委托、close 顺序 W1/F/C、write-after-close、截断、构造面、10KB 内容、子区域）。

### 版本差异（实测锁定，写入本侧断言）
- close 幂等：JDK-27 的 FilterOutputStream 基类有 closed 标志，第二次 close 为无操作（closes==1, flushes==1）；JDK-8 为 2/2。双侧各自断言实测行为。

### 负例精确异常表（双侧实测）
同 JDK-8 侧（AIOOBE arraycopy 路径 / NPE / "Buffer size <= 0"）。
