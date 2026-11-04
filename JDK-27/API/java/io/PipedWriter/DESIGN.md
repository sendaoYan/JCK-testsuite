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
