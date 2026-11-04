# java.io.PipedInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`() `(默认容量 PIPE_SIZE=1024)、`(int pipeSize)`、`(PipedOutputStream)`、`(PipedOutputStream, int)`；`connect(PipedOutputStream)`。
- 读：`read()`、`read(byte[],int,int)`、`available()`（缓冲计数）；protected `receive(int/byte[],off,len)` 由写端调用。
- 语义：未连接读抛 "Pipe not connected"；读者关闭后读抛 "Pipe closed"、写者 receive 抛 "Pipe closed"；写端关闭→EOF；循环缓冲满时写者阻塞等待读者。

## 2. Java 用户如何使用
- 线程间字节管道：生产者 `new PipedOutputStream(sink)` 写，消费者读；close 由写端收尾通知 EOF。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 循环缓冲与 checkStateForReceive/awaitSpace — `jdk25u-dev/src/java.base/share/classes/java/io/PipedInputStream.java:64-73`（DEFAULT_PIPE_SIZE/PIPE_SIZE）。
- 官方回归测试 — `jdk25u-dev/test/jdk/java/io/PipedInputStream/`。

## 4. 测试用例设计 + 自动化实现（PipedInputStreamTest.testBlackBox）
1. 未连接 read 抛 "Pipe not connected"。
2. 单写 daemon 线程 + 主线程读满 601 字节到 EOF，join 收尾（不 sleep 断时序）；有序无丢失。
3. available 随写/读精确增减（3→消费 1→2）。
4. 5000 字节跨越默认 1024 缓冲的 wraparound 全序读回。
5. 读者 close 后写抛 IOException、读抛 "Pipe closed"；二次 connect 抛 IOException。
6. bulk read 循环填满 4 字节。
- 健壮性: 单写者单读者 daemon 线程 + join(5000) 收尾；无全局流污染。

产出：`PipedInputStreamTest.java`（白盒 + 黑盒共 23 断言）。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testCtorAndConnectMatrix`：pipeSize 边界 IAE（-1/0 → "Pipe Size <= 0"）；PIPE_SIZE 行为探针 =1024（模块封闭禁止 setAccessible，改为默认管道恰好写 1024 字节不阻塞且 available()==1024；modifiers 仍反射断言 protected static final）；connect(null) NPE（27 带 helpful message）；双向重复 connect / 第三者 connect / 已连接构造器 → 精确消息 "Already connected"；`(src, pipeSize)` 构造连接且缓冲生效；**未连接 close 后 connect 仍成功**（字节侧 connect 无 closed 检查），但随后读写均 "Pipe closed"。
2. `testReadBoundsAndZero`：参数校验顺序——`read(b,0,0)` 未连接**返回 0 不抛**（bounds/len==0 先于连接检查）；`read(b,-1,0)` 未连接抛 IOOBE；`read(null,0,1)` NPE；connected 越界 IOOBE（27 消息含 "out of bounds"）；reader 自身 close 后 len==0 仍 0、len>0 抛 "Pipe closed"；writer close 后 read(b,0,1) = -1。
3. `testAvailableContract`：未连接 0；closed 0；缓冲恰好写满时 available()==pipeSize；排空 0。
4. `testReceiveProtected`：子类暴露 protected `receive(int)`：未连接 "Pipe not connected"、writer close 后 "Pipe closed"、正常路径缓冲 1 字节可读。
5. `testBrokenPipeDetection`：受控 daemon 线程 + join(15000)/latch：写线程死亡（不 close）→ 缓冲字节仍可读、排空后 `read()` 立即抛 "Write end dead"；读线程死亡（不 close）→ 写端立即抛 "Read end dead"。入口检查直抛，确定性。
6. `testSkipAndMark`：markSupported false；skip 负/0 → 0、skip(2)、EOF 后 0、越余量返回实际剩余数。
7. `testSingleThreadAndUtf8`：同线程写后读：0xCA 读回 unsigned 202；UTF-8 拆批写跨读重组 "héllo"；available 精确增减。
8. `testCrossThreadLatchedRoundTrip`：256 缓冲 × 4096 字节多回绕 + 背压，latch 收尾全序无丢失。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 read()/write(b,..)/receive(int) | IOException | Pipe not connected | 同 |
| 重复 connect（任一方向/第三者/构造器） | IOException | Already connected | 同 |
| 任一端 close 后的读写 | IOException | Pipe closed | 同 |
| 写线程死亡后（排空）read | IOException | Write end dead | 同 |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| pipeSize -1/0 | IllegalArgumentException | Pipe Size <= 0 | 同 |
| read 越界 | IndexOutOfBoundsException | null | `Range [x, x+y) out of bounds for length n`（Objects.checkFromIndexSize） |
| read(null,0,1) | NullPointerException | null | null |
| pis.connect(null) | NullPointerException | null | `Cannot invoke "java.io.PipedOutputStream.connect(java.io.PipedInputStream)" because "src" is null` |

JDK-27 特有断言/差异：
- IOOBE 消息含 "out of bounds"、pis.connect(null) NPE 含 'because "src" is null'（均 27 侧专属断言；8 侧为 null 消息只断类型）。
- PIPE_SIZE 值改行为探针（InaccessibleObjectException 规避）。
- 其余行为与消息与 JDK-8 完全一致（源码 diff 证实仅 javadoc/实现现代化）。
- "Pipe broken"（读端阻塞中写端死亡）时序依赖，不做确定性断言，仅记录。

