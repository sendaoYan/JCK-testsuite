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
