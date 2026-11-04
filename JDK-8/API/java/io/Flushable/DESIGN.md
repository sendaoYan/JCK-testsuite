# java.io.Flushable 测试方案 (DESIGN)

## 1. API 功能
- 声明 `flush() throws IOException`：强制缓冲输出落底。
- OutputStream 与 Writer 皆实现；无缓冲实现 flush 为无操作；close 通常也 flush。

## 2. Java 用户如何使用
- 写出后 `flush()` 确保数据到达底层 sink；BufferedOutputStream/Writer 依赖它排空缓冲。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/Flushable.java`。

## 4. 测试用例设计 + 自动化实现（FlushableTest.java，纯黑盒）
黑盒场景:
1. lambda 实现多次 flush 计数。
2. BufferedOutputStream 未 flush 前字节不达底层 Recorder；flush 后到达。
3. flush 失败 IOException 经接口传播。
4. 无缓冲流（ByteArrayOutputStream）flush 无害无操作。
5. close 排空缓冲（BufferedOutputStream）。
6. flush 可重复，每次都被计数实现观察到。
- 健壮性: 确定性 Recorder sink；无反射白盒。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（行为一致）。

### 新增维度
- **TWR 不自动 flush**：同时实现 Closeable+Flushable 的计数 sink —— TWR 结束 closes==1 且 flushes==0（仅显式 flush 计数）；接口契约层面 close 不隐含 flush。
- **字符侧 flush 可观察**：BufferedWriter(8192) 经 Flushable 引用 flush 前字符滞留缓冲、flush 后到达底层 sink；重复 flush 幂等。
- **flush after close**：BufferedWriter close 后 flush → IOException("Stream closed")（逐字双侧实测）。
- 文档化 no-op 实现：StringWriter/CharArrayWriter 经接口反复 flush 安全且后续可用。
- 失败传播：自定义 Writer flush 抛 IOE，经 Flushable 引用调用原消息传播。
- 反射白盒：根接口（无 extends）、flush 抽象且 throws IOException、OutputStream/Writer 实现、与 Closeable 无继承关系。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| 自定义失败 flush | IOException | 原消息透传 |
| BufferedWriter close 后 flush | IOException | "Stream closed" |

### 版本差异
- 无。断言 9 → 28。
