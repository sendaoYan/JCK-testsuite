# java.io.Flushable 测试方案 (DESIGN)

## 1. API 功能
- `flush() throws IOException`：把缓冲数据强制写到下层。

## 2. Java 用户如何使用
- 缓冲流（BufferedOutputStream/Writer）在关键时刻 flush 落盘/落网；接口化统一调用。

## 3. 开源代码实际用法调研
- 定义 — `jdk25u-dev/src/java.base/share/classes/java/io/Flushable.java`。
- 语义 — `jdk25u-dev/src/java.base/share/classes/java/io/BufferedOutputStream.java`（flush 把 buf 写入下层）。

## 4. 测试用例设计 + 自动化实现（FlushableTest.java）
- 白盒: 接口、根接口（无父）、flush 声明 IOException、OutputStream/Writer 实现它。
- 黑盒场景:
  1. lambda 计数。
  2. 缓冲流写入未到下层、flush 后到达（可观测下沉）。
  3. flush 抛 IOException 传播。
  4. 无缓冲流 flush no-op。
  5. close 也会 flush 缓冲。
  6. flush 可重复。
- 健壮性: 全内存。

产出：`FlushableTest.java`（白盒 7 + 黑盒 7 断言）。

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
- 无。断言 15 → 27（白盒已在既有 testWhiteBox）。
