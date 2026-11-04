# java.io.PipedReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`() `(默认 1024)、`(int)`、`(PipedWriter)`、`(PipedWriter,int)`；`connect(PipedWriter)`。
- 读：`read()`、`read(char[],int,int)`、`skip`、`ready()`（缓冲非空）；未连接抛 "Pipe not connected"；读端关闭 "Pipe closed"；写端关闭→EOF。

## 2. Java 用户如何使用
- 线程间字符管道：文本生产者/消费者解耦；写端 close 后 read 返回 -1 而非阻塞。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 缓冲/连接/awaitSpace 实现 — `jdk25u-dev/src/java.base/share/classes/java/io/PipedReader.java:52,175,179,240`（DEFAULT_PIPE_SIZE、"Pipe not connected"、"Read end dead"）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/PipedReader/`。

## 4. 测试用例设计 + 自动化实现（PipedReaderTest.testBlackBox）
1. 未连接 read 抛 "Pipe not connected"。
2. 单写 daemon 写 a..z + close，主线程读到 EOF 全序收全（join 收尾）。
3. 区段 read(buf,1,3) 只填 [1,4)；ready() 空假有真随消费翻转。
4. 写端 close→EOF 且持续 -1；读端 close 后 read 抛 "Pipe closed"、写端再写抛。
5. 二次 connect 抛 IOException；skip(2) 后续读正确；size-1 管道配 daemon 写者双字符往返（写满阻塞需活读者——"Read end dead" 语义规避）。
- 健壮性: 每场景独立管道、daemon+join 收尾。

产出：`PipedReaderTest.java`（白盒 + 黑盒共 24 断言）。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testCtorAndConnectMatrix`：pipeSize IAE 消息 "Pipe size <= 0"（与字节版 "Pipe Size <= 0" 大小写不同）；connect(null) NPE（27 带 helpful message）；双向重复 connect / 第三者 / 已连接构造器 → "Already connected"；**字符版特有 closed 检查：未连接 close 后 connect → "Pipe closed"（字节版是 connect 成功）**；writer 的 connect 与构造器对 closed-unconnected reader → "Pipe closed"；was-connected writer close 后 connect(other) → "Already connected"；`(src, 4)` 构造缓冲生效。
2. `testReadBoundsAndOrder`：**校验顺序与字节版相反——连接/关闭检查先于 bounds 与 len==0**：`read(c,0,0)`/`read(c,-1,0)` 未连接抛 "Pipe not connected"；connected 越界 IOOBE（27 消息含 "out of bounds"）；close 后 len==0 也抛 "Pipe closed"；writer close 后 read(c,0,1) = -1。
3. `testReadyContract`：未连接/closed 抛；空 false；缓冲 true；排空 false；EOF false。
4. `testBrokenPipeDetection`：受控 daemon 线程：写线程死亡 → read()/read(c,0,1)/ready() 三入口均 "Write end dead"；读线程死亡 → write "Read end dead"。
5. `testSkipContract`：skip(1) 未连接 "Pipe not connected"；skip(-3) IAE "skip value is negative"（与 InputStream.skip 返回 0 的族差异）；skip(0)=0、EOF 0、越余量实际值。
6. `testCharRange`：0x1234/0xFFFF 16 位全域无损。
7. `testCrossThreadLatchedRoundTrip`：64 缓冲 × 2048 字符多回绕 + 背压。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 read()/ready()/skip()/read(c,0,0) | IOException | Pipe not connected | 同 |
| 重复 connect | IOException | Already connected | 同 |
| close 后 read/ready（含 len==0） | IOException | Pipe closed | 同 |
| closed-unconnected 端 connect | IOException | Pipe closed | 同（字节版此场景为成功） |
| 写线程死亡后 read/read(c)/ready | IOException | Write end dead | 同 |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| pipeSize -1/0 | IllegalArgumentException | Pipe size <= 0 | 同 |
| read 越界（connected） | IndexOutOfBoundsException | null | `Range [x, x+y) out of bounds for length n`（Objects.checkFromIndexSize） |
| skip(负数) | IllegalArgumentException | skip value is negative | 同 |
| pr.connect(null) | NullPointerException | null | `Cannot invoke "java.io.PipedWriter.connect(java.io.PipedReader)" because "src" is null` |

JDK-27 特有断言/差异：
- IOOBE 消息含 "out of bounds"、pr.connect(null) NPE 含 'because "src" is null'（27 侧专属断言；8 侧只断类型）。
- 行为与 IOException/IAE 消息与 JDK-8 完全一致。
- PipedReader.receive 包私有，无法子类暴露直测（经 PipedWriter.write 间接触发）。

