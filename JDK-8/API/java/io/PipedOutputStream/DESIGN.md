# java.io.PipedOutputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造：`()`、`(PipedInputStream)`；`connect(PipedInputStream)`；`write(int)`、`write(byte[],int,int)`、`flush()`、`close()`。
- 语义：未连接写抛 "Pipe not connected"；读端关闭后写/flush 抛 "Pipe closed"；close 交付 EOF。

## 2. Java 用户如何使用
- 生产者线程写入，消费者 PipedInputStream 读取；close() 通知对端 EOF。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PipedOutputStream.java`（sink 引用、flush 通知）。

## 4. 测试用例设计 + 自动化实现（PipedOutputStreamTest.java，纯黑盒）
黑盒场景:
1. 未连接写 → "Pipe not connected"；未连接 flush 静默无操作。
2. 主线程写 100 字节 + 单读线程排空 + close EOF + 顺序保持。
3. 批量 write 4 字节被默认缓冲吸收。
4. 读端关闭后写 → "Pipe closed"。
5. 二次 connect 拒绝；close 幂等 + close 后写失败。
6. write(byte[],off,len) 只写子区间（对端 available=3 + 内容）。
- 健壮性: daemon 线程 + join 超时；无反射白盒。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testWhiteBox`（JDK-8 侧新增轻量白盒）：构造器/connect/write/flush 方法表 + private sink 字段。
2. `testCtorAndConnectMatrix`：connect(null) 显式 NPE（**双侧消息均为 null**，与 pis.connect(null) 的 JVM 隐式 NPE 不同）；双向重复 connect / 第三者 connect → "Already connected"；was-connected close 后 connect(other) → "Already connected"（sink 仍非 null 先判）；**unconnected close 是真空操作**（sink==null），之后 connect 成功且可正常往返（字节侧 connect 无 closed 检查）。
3. `testWriteBoundsAndZero`：校验顺序——`write(b,0,0)` 未连接仍抛 "Pipe not connected"（sink 检查先于 len==0，与 PIS.read(b,0,0) 返回 0 **不对称**）；`write(null,0,1)` connected NPE；越界 IOOBE（-1/0 负/len>remaining/Integer.MAX_VALUE 溢出）；**len==0 在自身 close 后与对端 close 后均静默返回**（len==0 先于 receive 状态检查）。
4. `testFlushNeverThrows`：flush 未连接 / reader close 后 / 自身 close 后三态均不抛——**与 PipedWriter.flush 抛 "Pipe closed" 的关键族差异**（字节版 flush 仅 notifyAll）。
5. `testWriteAfterCloseContract`：close 幂等；close 后 write(int)/write(b,0,1) → 精确消息 "Pipe closed"。
6. `testClosedUnconnectedPeer`：connect 到 closed-unconnected 的 PipedInputStream 成功，随后 write → "Pipe closed"。
7. `testReadEndDead`：受控 daemon 读线程消费后死亡（不 close）→ write 立即抛 "Read end dead"（latch + join(15000)，确定性）。
8. `testUnsignedAndFullArray`：write(0xFF) → 对端读回 255（unsigned）；write(byte[]) 全数组精确值。
9. `testCrossThreadBigPayload`：1024 默认缓冲 × 8192 字节分块写（写端阻塞由并发读者排解）+ flush + close EOF，latch 收尾全序无丢失。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 write(int)/write(b,*,*)（含 len==0） | IOException | Pipe not connected | 同 |
| 重复 connect（任一方向/第三者） | IOException | Already connected | 同 |
| close 后 write（自身或对端） | IOException | Pipe closed | 同 |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| write 越界（负/溢出/len>remaining） | IndexOutOfBoundsException | null | `Range [x, x+y) out of bounds for length n` |
| write(null,0,1) connected | NullPointerException | null | null |
| pos.connect(null)（显式抛） | NullPointerException | null | null |

JDK-8 vs JDK-27 差异记录：
- 行为与全部 IOException 消息双侧一致。
- 27 侧 `sink` 字段声明为 **volatile**（8 侧普通字段）——27 侧 testJdk27Structure 白盒断言。
- 27 侧 IOOBE 带 checkFromIndexSize 消息、8 侧 null，两侧各自断言。

