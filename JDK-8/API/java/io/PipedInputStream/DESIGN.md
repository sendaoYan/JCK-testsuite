# java.io.PipedInputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造：`()`、`(int pipeSize)`、`(PipedOutputStream)`、`(PipedOutputStream, int)`；`connect(PipedOutputStream)`。
- 读：`read()`、`read(byte[],int,int)`、`available()`；`close()`。
- 语义：未连接读抛 "Pipe not connected"；写端关闭交付 EOF(-1)；读端关闭后读/写抛 "Pipe closed"。

## 2. Java 用户如何使用
- 生产者-消费者线程间字节传递：`PipedOutputStream out = new PipedOutputStream(in);` 写线程 write，读线程 read 到 EOF。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PipedInputStream.java`（环形缓冲、connect 双向校验）。

## 4. 测试用例设计 + 自动化实现（PipedInputStreamTest.java，纯黑盒）
黑盒场景:
1. 未连接读 → "Pipe not connected"。
2. 单写线程 + 主线程排空往返 601 字节 + 哨兵 + writer 结束。
3. available 反映缓冲字节（3→消费→2）。
4. 环形缓冲回绕：5000 字节精确有序。
5. 读端关闭后写失败；读端自身关闭后读抛 "Pipe closed"。
6. 二次 connect 拒绝。
7. read(byte[],off,len) 批量排空 + EOF。
- 健壮性: daemon 线程 + join 超时，无墙钟依赖；无反射白盒。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testCtorAndConnectMatrix`：pipeSize 边界 IAE（-1/0 → "Pipe Size <= 0"）；PIPE_SIZE 反射断言 =1024 且 protected static final；connect(null) NPE；双向重复 connect / 第三者 connect / 已连接构造器 → 精确消息 "Already connected"；`(src, pipeSize)` 构造连接且缓冲生效；**未连接 close 后 connect 仍成功**（字节侧 connect 无 closed 检查），但随后读写均 "Pipe closed"。
2. `testReadBoundsAndZero`：参数校验顺序——`read(b,0,0)` 未连接**返回 0 不抛**（bounds/len==0 先于连接检查）；`read(b,-1,0)` 未连接抛 IOOBE（同因）；`read(null,0,1)` NPE；connected 越界 IOOBE；reader 自身 close 后 len==0 仍 0、len>0 抛 "Pipe closed"；writer close 后 read(b,0,1) = -1。
3. `testAvailableContract`：未连接 0；closed 0；缓冲恰好写满时 available()==pipeSize（in==out 表满）；排空 0。
4. `testReceiveProtected`：子类暴露 protected `receive(int)`：未连接 "Pipe not connected"、writer close 后 "Pipe closed"、正常路径缓冲 1 字节可读。
5. `testBrokenPipeDetection`：受控 daemon 线程 + join(15000)/latch，无 sleep 轮询：写线程死亡（不 close）→ 缓冲字节仍可读、排空后 `read()` 立即抛 "Write end dead"；读线程死亡（不 close）→ 写端立即抛 "Read end dead"。两路径均入口检查直抛、零等待，确定性。
6. `testSkipAndMark`：markSupported false；InputStream.skip 默认语义：负/0 → 0、skip(2) 跳 2 后续读正确、EOF 后 0、越余量返回实际剩余数。
7. `testSingleThreadAndUtf8`：同线程写后读（数据在缓冲内合法，记录此契约）：0xCA 读回 unsigned 202；"héllo" UTF-8 字节拆两批写跨读重组完整；available 随读写精确增减。
8. `testCrossThreadLatchedRoundTrip`：256 缓冲 × 4096 字节（强制多次环形回绕 + 写端背压等待），写线程 latch 收尾，全部有序无丢失。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 read()/write(b,..)/receive(int) | IOException | Pipe not connected | 同 |
| 重复 connect（任一方向/第三者/构造器） | IOException | Already connected | 同 |
| 任一端 close 后的读写 | IOException | Pipe closed | 同 |
| 写线程死亡后（排空）read | IOException | Write end dead | 同 |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| pipeSize -1/0 | IllegalArgumentException | Pipe Size <= 0 | 同 |
| read(b,-1,0)/read(b,0,-1)/read(b,2,3) | IndexOutOfBoundsException | null（无消息） | `Range [x, x+y) out of bounds for length n` |
| read(null,0,1) | NullPointerException | null | null |
| pis.connect(null)（委托触发） | NullPointerException | null | `Cannot invoke "java.io.PipedOutputStream.connect(java.io.PipedInputStream)" because "src" is null` |

JDK-8 vs JDK-27 差异记录：
- 全部 IOException/IAE 消息与行为双侧一致（源码 diff 证实仅实现现代化）。
- IOOBE/NPE：27 带 helpful message（Objects.checkFromIndexSize / JVM null 描述），8 为 null——两侧各自断言。
- 27 模块系统禁止 setAccessible(java.base 字段)：PIPE_SIZE 值在 27 侧改用行为探针（默认管道恰好写 1024 不阻塞且 available()==1024）。
- "Pipe broken"（读端阻塞中写端死亡，~2s trials 轮次后抛）依赖时序，按约定不做确定性断言，此处仅记录。

