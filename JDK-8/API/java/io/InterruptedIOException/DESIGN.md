# java.io.InterruptedIOException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；公共可变字段 `int bytesTransferred`。

## 2. Java 用户如何使用
- 阻塞读被中断时捕获；读取 bytesTransferred 记录已传输字节数。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/InterruptedIOException.java`；`PipedInputStream.read` 中断抛出。

## 4. 测试用例设计 + 自动化实现（InterruptedIOExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息/默认 bytesTransferred==0。
2. bytesTransferred 可写（文档化部分传输）。
3. 中断阻塞在空 PipedInputStream.read 的线程 → InterruptedIOException，bytesTransferred==0，中断状态被消耗。
4. 普通读不受影响（对比）。
5. 作为 IOException 捕获。
- 健壮性: daemon 线程 + join 超时；无反射白盒。
