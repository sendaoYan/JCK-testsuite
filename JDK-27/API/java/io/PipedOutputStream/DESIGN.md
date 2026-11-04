# java.io.PipedOutputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`() `、`(PipedInputStream)`；`connect(PipedInputStream)`。
- `write(int)`、`write(byte[],int,int)`（sink 未连接抛 "Pipe not connected"）、`flush()`（唤醒读者；未连接为静默 no-op）、`close()`（sink.receivedLast 通知 EOF）。

## 2. Java 用户如何使用
- 管道生产端：写完 flush 立即可见，close 通知消费者 EOF。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- write/connect/flush/close 实现 — `jdk25u-dev/src/java.base/share/classes/java/io/PipedOutputStream.java:121-167`（"Pipe not connected" 与 sink.notifyAll）。

## 4. 测试用例设计 + 自动化实现（PipedOutputStreamTest.testBlackBox）
1. 未连接 write 抛 "Pipe not connected"；未连接 flush 为 no-op（sink==null 短路，源码实证）。
2. 主线程写 1..100 + flush + close，单 daemon 读线程收满有序退出（join 收尾）。
3. 区段 write(b,1,3) 只写 [1,4)；小量写默认缓冲吸收无需读者。
4. 读者 close 后 write 抛 "Pipe closed"；二次 connect 抛 IOException；close 幂等、close 后 write 抛。
- 健壮性: 单写单读 + join；纯内存管道。

产出：`PipedOutputStreamTest.java`（白盒 + 黑盒共 19 断言）。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testJdk27Structure`：JDK-27 特有——`sink` 声明为 **volatile**（JDK-8 为普通 private 字段），仍为 private PipedInputStream 类型。
2. `testCtorAndConnectMatrix`：connect(null) 显式 NPE（双侧消息均 null）；双向重复 connect / 第三者 connect → "Already connected"；was-connected close 后 connect(other) → "Already connected"；**unconnected close 是真空操作**，之后 connect 成功且可往返（字节侧 connect 无 closed 检查）。
3. `testWriteBoundsAndZero`：`write(b,0,0)` 未连接仍抛 "Pipe not connected"（sink 检查先于 len==0，与 PIS.read(b,0,0) 返回 0 不对称）；write(null,0,1) NPE；越界 IOOBE（27 消息含 "out of bounds"，含 MAX_VALUE 溢出例）；len==0 在双侧 close 后均静默。
4. `testFlushNeverThrows`：flush 未连接 / reader close 后 / 自身 close 后三态均不抛——与 PipedWriter.flush 抛 "Pipe closed" 的关键族差异。
5. `testWriteAfterCloseContract`：close 幂等；close 后 write(int)/write(b,0,1) → "Pipe closed"。
6. `testClosedUnconnectedPeer`：connect 到 closed-unconnected PipedInputStream 成功，随后 write → "Pipe closed"。
7. `testReadEndDead`：受控 daemon 读线程死亡后 write → "Read end dead"（latch + join，确定性）。
8. `testUnsignedAndFullArray`：0xFF → 255 unsigned；write(byte[]) 全数组。
9. `testCrossThreadBigPayload`：8192 字节分块 + flush + close EOF，latch 全序。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 write（含 len==0） | IOException | Pipe not connected | 同 |
| 重复 connect | IOException | Already connected | 同 |
| close 后 write | IOException | Pipe closed | 同 |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| write 越界 | IndexOutOfBoundsException | null | `Range [x, x+y) out of bounds for length n`（Objects.checkFromIndexSize） |
| write(null,0,1) connected | NullPointerException | null | null（27 仍保留显式 null 检查） |
| pos.connect(null) | NullPointerException | null | null |

JDK-27 特有断言/差异：
- `sink` volatile 白盒断言（testJdk27Structure）。
- IOOBE 消息含 "out of bounds"（27 侧专属；8 侧 null 只断类型）。
- 行为与 IOException 消息与 JDK-8 完全一致。

