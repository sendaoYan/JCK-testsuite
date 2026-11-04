# java.io.PipedReader 测试方案 (DESIGN)

## 1. API 功能
- 构造：`()`、`(int pipeSize)`、`(PipedWriter)`、`(PipedWriter, int)`；`connect(PipedWriter)`。
- 读：`read()`、`read(char[],int,int)`、`ready()`、`skip(long)`；`close()`。

## 2. Java 用户如何使用
- 线程间字符传递：写线程 PipedWriter.write，读线程 PipedReader.read 到 EOF；ready() 判断是否已有数据。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PipedReader.java`。

## 4. 测试用例设计 + 自动化实现（PipedReaderTest.java，纯黑盒）
黑盒场景:
1. 未连接读 → "Pipe not connected"。
2. 单写线程 a-z + 主线程排空 + EOF。
3. read(char[],off,len) 区域填充、未触槽保持 0。
4. ready() 空/有数据/排空三态。
5. 写端 close 交付 EOF。
6. 读端 close 后读抛 "Pipe closed"；写端向已关闭读端写失败。
7. 二次 connect 拒绝。
8. skip 前进 + read 续读。
9. 小管道(1) 在读者排空时往返两字符。
- 健壮性: daemon 线程 + join 超时；无反射白盒。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testWhiteBox`（JDK-8 侧新增轻量白盒）：构造器 4 个/connect/ready 方法表 + package-visible char[] buffer。
2. `testCtorAndConnectMatrix`：pipeSize IAE 消息 **"Pipe size <= 0"（与字节版 "Pipe Size <= 0" 大小写不同）**；connect(null) NPE；双向重复 connect / 第三者 / 已连接构造器 → "Already connected"；**字符版特有 closed 检查：未连接 close 后 connect → "Pipe closed"（字节版是 connect 成功）**；writer 的 connect 与构造器对 closed-unconnected reader → "Pipe closed"；was-connected writer close 后 connect(other) → "Already connected"（sink 先判）；`(src, 4)` 构造连接且缓冲生效。
3. `testReadBoundsAndOrder`：**校验顺序与字节版相反——连接/关闭检查先于 bounds 与 len==0**：`read(c,0,0)`/`read(c,-1,0)` 未连接抛 "Pipe not connected"（PIS.read(b,0,0) 是返回 0）；connected 越界 IOOBE；reader 自身 close 后 len==0 也抛 "Pipe closed"；writer close 后 read(c,0,1) = -1。
4. `testReadyContract`：未连接 → "Pipe not connected"；closed → "Pipe closed"；空（活写端）false；缓冲 true；排空 false；写端 close（EOF）false。
5. `testBrokenPipeDetection`：受控 daemon 线程 + join/latch：写线程死亡（不 close）→ 缓冲字符仍可读、排空后 `read()`/`read(c,0,1)`/**`ready()` 三处入口检查**均立即抛 "Write end dead"；读线程死亡（不 close）→ 写端立即抛 "Read end dead"。
6. `testSkipContract`：skip(1) 未连接 → "Pipe not connected"（Reader.skip 委托 read）；**skip(-3) → IAE "skip value is negative"（与 InputStream.skip 负数返回 0 的族差异）**；skip(0)=0；skip(2) 跳 2 续读 'c'；写端 close 后越余量 skip(99)=3、EOF 后 0。
7. `testCharRange`：char 16 位全域传递：0x1234 与 0xFFFF 均无损（字节版仅 0-255）。
8. `testCrossThreadLatchedRoundTrip`：64 缓冲 × 2048 字符多回绕 + 背压，latch 收尾全序无丢失。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 read()/ready()/skip()/read(c,0,0) | IOException | Pipe not connected | 同 |
| 重复 connect（任一方向/第三者/构造器） | IOException | Already connected | 同 |
| close 后 read/ready（含 len==0） | IOException | Pipe closed | 同 |
| closed-unconnected 端 connect（双向/构造器） | IOException | Pipe closed | 同（字节版此场景为成功） |
| 写线程死亡后（排空）read/read(c)/ready | IOException | Write end dead | 同 |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| pipeSize -1/0 | IllegalArgumentException | Pipe size <= 0 | 同 |
| read 越界（connected） | IndexOutOfBoundsException | null | `Range [x, x+y) out of bounds for length n` |
| read(null,0,1) | NullPointerException | null | null |
| skip(负数) | IllegalArgumentException | skip value is negative | 同 |
| pr.connect(null) | NullPointerException | null | `Cannot invoke "java.io.PipedWriter.connect(java.io.PipedReader)" because "src" is null` |

JDK-8 vs JDK-27 差异记录：
- 行为与全部 IOException/IAE 消息双侧一致。
- 27 侧 IOOBE 带 checkFromIndexSize 消息、pr.connect(null) 带 null 描述；8 侧为 null——两侧各自断言。
- PipedReader.receive 为包私有（非 protected），无法像 PipedInputStream 那样子类暴露直测——通过 PipedWriter.write 间接触发同路径。

