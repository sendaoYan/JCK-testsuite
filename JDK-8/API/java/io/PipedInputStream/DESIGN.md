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
