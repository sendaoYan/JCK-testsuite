# java.io.InterruptedIOException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `()`：线程在 I/O 等待中被中断；公共字段 `bytesTransferred` 报告中断前已传输的字节数。

## 2. Java 用户如何使用
- 可中断的阻塞读（如 PipedInputStream.read 在 wait 中被 interrupt）抛出；调用方据此恢复中断状态或清理。

## 3. 开源代码实际用法调研
- PipedInputStream.read 的 InterruptedException→InterruptedIOException 转换（不重设中断位）— `jdk25u-dev/src/java.base/share/classes/java/io/PipedInputStream.java:281-283,334-336`。
- bytesTransferred 语义 — `jdk25u-dev/src/java.base/share/classes/java/io/InterruptedIOException.java`。

## 4. 测试用例设计 + 自动化实现（InterruptedIOExceptionTest.java）
- 白盒: extends IOException、public int bytesTransferred、两构造。
- 黑盒场景:
  1. 构造器与 bytesTransferred 默认 0。
  2. bytesTransferred 可赋值（部分传输语义）。
  3. 真实触发：daemon 读线程阻塞于空管道 read()，主线程轮询到 WAITING/TIMED_WAITING 后 interrupt + join，断言抛出 InterruptedIOException、bytesTransferred==0、中断位未被重设（源码不调用 re-interrupt）。
  4. 对照：普通读不受影响。
  5. 可按 IOException 捕获。
- 健壮性: 单写单读、daemon 线程 join 收尾，无 sleep 断言时序；finally close 双端。

产出：`InterruptedIOExceptionTest.java`（白盒 5 + 黑盒 8 断言）。
