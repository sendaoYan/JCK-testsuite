# java.io.PipedWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`() `、`(PipedReader)`；`connect(PipedReader)`。
- `write(int)`、`write(char[],int,int)`、`write(String,int,int)`（未连接抛 "Pipe not connected"）；`flush()`（未连接 no-op；JDK 27 中已关闭管道 flush 抛 "Pipe closed"）；`close()`→receivedLast 通知 EOF。

## 2. Java 用户如何使用
- 字符管道生产端：写字符串区段、flush 唤醒读者、close 结束。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- write/flush/close 实现 — `jdk25u-dev/src/java.base/share/classes/java/io/PipedWriter.java:102,125-127,155-173`（"Pipe closed"、"Pipe not connected"）。

## 4. 测试用例设计 + 自动化实现（PipedWriterTest.testBlackBox）
1. 未连接 write 抛 "Pipe not connected"；未连接 flush 静默 no-op（源码 null 短路）。
2. 主线程写 '0'..'9'+write("suffix")+flush+close，单 daemon 读线程 16 字符全序收满（join 收尾）。
3. write(String,2,2) 只发 "cd"；append("ok") 经 Writer 路由。
4. 读端 close 后 write 抛 "Pipe closed"、flush 抛 "Pipe closed"（JDK 27 实证）；二次 connect/close 幂等/close 后 write 抛。
- 健壮性: 单写单读 + join；无全局流。

产出：`PipedWriterTest.java`（白盒 + 黑盒共 20 断言）。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testCtorAndConnectMatrix`：connect(null) 显式 NPE（双侧消息 null）；双向重复 connect / 第三者 → "Already connected"；**字符版特有 closed 语义：unconnected close 置位 closed 标志后 connect → "Pipe closed"（字节版 unconnected close 后可连接）**；connect 与构造器对 closed-unconnected reader → "Pipe closed"；was-connected close 后 connect(other) → "Already connected"。
2. `testWriteBoundsAndZero`：write(c,0,0) 未连接仍抛 "Pipe not connected"；write(null cbuf,0,1) NPE（27 消息 'Cannot read the array length'）；越界 IOOBE（27 消息含 "out of bounds"）；len==0 在双侧 close 后均静默（receive(char[],off,0) 循环不执行）。
3. `testStringAndAppend`：write(String,-1,1)/(2,2)/(1,-1) → StringIndexOutOfBoundsException（IOOBE 子类；双侧消息格式不同故断言类型级）；write((String)null) NPE（27 消息 '"str" is null'）；write((String)null,0,1) NPE；append(CharSequence) 返回 this 且入管；append(null) 写出 "null"；append(null,1,3) 写出 "ul"。
4. `testCloseFlushContract`：close 幂等；自身 close 后 write/flush → "Pipe closed"；reader close 后 write/flush → "Pipe closed"（与 PipedOutputStream.flush 从不抛的族差异）。
5. `testReadEndDead`：受控 daemon 读线程死亡后 write → "Read end dead"（latch + join，确定性）。
6. `testCrossThreadLatchedRoundTrip`：1024 字符分块 + flush + close EOF，latch 全序。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 write（含 len==0） | IOException | Pipe not connected | 同 |
| 重复 connect | IOException | Already connected | 同 |
| close 后 write/flush | IOException | Pipe closed | 同 |
| closed-unconnected 端 connect | IOException | Pipe closed | 同（字节版 unconnected 侧为成功） |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| write(char[]) 越界 | IndexOutOfBoundsException | null | `Range [x, x+y) out of bounds for length n`（Objects.checkFromIndexSize） |
| write(String) 越界 | StringIndexOutOfBoundsException | `String index out of range: n` | `Range [x, y) out of bounds for length n` |
| write(null cbuf) | NullPointerException | null | `Cannot read the array length because "cbuf" is null` |
| write((String)null) | NullPointerException | null | `Cannot invoke "String.length()" because "str" is null` |
| pw.connect(null) | NullPointerException | null | null |

JDK-27 特有断言/差异：
- NPE/IOOBE helpful message 子串断言（"Cannot read the array length" / '"str" is null' / "out of bounds"）——8 侧为 null 消息只断类型。
- 行为与 IOException 消息与 JDK-8 完全一致；sink 两侧均非 volatile（与 POS 的 27 侧 volatile 不同）。

