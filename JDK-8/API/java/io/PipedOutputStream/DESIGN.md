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
