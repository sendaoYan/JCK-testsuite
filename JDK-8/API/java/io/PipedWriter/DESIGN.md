# java.io.PipedWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造：`()`、`(PipedReader)`；`connect(PipedReader)`；`write(int)`、`write(char[],int,int)`、`write(String,int,int)`、`flush()`、`close()`；继承 `append(...)`。

## 2. Java 用户如何使用
- 生产者线程写字符，消费者 PipedReader 读取；close() 通知对端 EOF。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PipedWriter.java`（sink 引用、flush 抛 "Pipe closed"）。

## 4. 测试用例设计 + 自动化实现（PipedWriterTest.java，纯黑盒）
黑盒场景:
1. 未连接写 → "Pipe not connected"；未连接 flush 静默无操作。
2. 主线程写 16 字符 + 单读线程排空 + close EOF + 顺序。
3. write(String,off,len) 切片 [2,4) 交付。
4. append(CharSequence) 经 write 路由。
5. 读端关闭后写 → "Pipe closed"。
6. 二次 connect 拒绝；close 幂等 + close 后写失败。
7. 读端关闭后 flush → "Pipe closed"。
- 健壮性: daemon 线程 + join 超时；无反射白盒。

## 5. 深化追加（契约级，批次 ioD；双侧探针实测后固化）

新增维度（对应新增方法）：
1. `testWhiteBox`（JDK-8 侧新增轻量白盒）：构造器/connect/write 三重载/flush 方法表 + private sink。
2. `testCtorAndConnectMatrix`：connect(null) 显式 NPE（双侧消息 null）；双向重复 connect / 第三者 → "Already connected"；**字符版特有 closed 语义：unconnected close 置位 closed 标志后 connect → "Pipe closed"（字节版 unconnected close 是真空操作后可连接）**；connect 与构造器对 closed-unconnected reader → "Pipe closed"；was-connected close 后 connect(other) → "Already connected"（sink 先判）。
3. `testWriteBoundsAndZero`：`write(c,0,0)` 未连接仍抛 "Pipe not connected"（sink 检查先于一切）；write(null cbuf,0,1) NPE（JDK-8 经 bounds 表达式的 cbuf.length 触发）；越界 IOOBE（-1/负/len>remaining）；**len==0 在自身 close 后与对端 close 后均静默**（receive(char[],off,0) 的 while(--len>=0) 不执行 → 不触发状态检查）。
4. `testStringAndAppend`：write(String,-1,1)/(2,2)/(1,-1) → StringIndexOutOfBoundsException（IOOBE 子类；**双侧消息格式不同**——8 为 "String index out of range: n"、27 为 "Range [x,y) out of bounds..."——故断言到 IOOBE 类型级）；write((String)null) NPE（Writer.write→str.length() 先炸，连接与否无关）；write((String)null,0,1) NPE；append(CharSequence) 返回 this 且字符确实入管；**append((CharSequence)null) 写出字面 "null"（4 字符）；append(null,1,3) 写出 "ul"**（Writer.append 的 null 字面量化语义）。
5. `testCloseFlushContract`：close 幂等；自身 close 后 write/flush → "Pipe closed"；reader close 后 write/flush → "Pipe closed"——**flush 抛异常是与 PipedOutputStream.flush 从不抛的关键族差异**。
6. `testReadEndDead`：受控 daemon 读线程消费后死亡（不 close）→ write 立即抛 "Read end dead"（latch + join，确定性）。
7. `testCrossThreadLatchedRoundTrip`：1024 默认缓冲 × 1024 字符分块写 + flush + close EOF，latch 收尾全序无丢失。

负例精确异常表（操作 | 类型 | JDK-8 实测消息 | JDK-27 实测消息）：
| 未连接 write（int/char[]/String，含 len==0） | IOException | Pipe not connected | 同 |
| 重复 connect | IOException | Already connected | 同 |
| close 后 write/flush（自身或对端） | IOException | Pipe closed | 同 |
| closed-unconnected 端 connect（双向/构造器） | IOException | Pipe closed | 同（字节版 unconnected 侧为成功） |
| 读线程死亡后 write | IOException | Read end dead | 同 |
| write(char[]) 越界 | IndexOutOfBoundsException | null | `Range [x, x+y) out of bounds for length n` |
| write(String) 越界 | StringIndexOutOfBoundsException（IOOBE 子类） | `String index out of range: n` | `Range [x, y) out of bounds for length n` |
| write(null cbuf/String) | NullPointerException | null | `Cannot read the array length because "cbuf" is null` / `Cannot invoke "String.length()" because "str" is null` |
| pw.connect(null)（显式抛） | NullPointerException | null | null |

JDK-8 vs JDK-27 差异记录：
- 行为与全部 IOException 消息双侧一致（sink 字段两侧均非 volatile，与 POS 的 27 侧 volatile 不同）。
- IOOBE/StringIOOBE/NPE 消息：27 带 helpful message、8 为 null/简式——String 越界只断类型，NPE 在 27 侧断言消息子串。
- write(String,off,len) 委托链：Writer.write(String) → str.getChars → this.write(char[],off,len)（即 PipedWriter.write(char[],int,int)），行为双侧一致。

