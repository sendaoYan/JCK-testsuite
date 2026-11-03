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
