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
