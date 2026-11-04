# java.io.PipedReader 测试方案 (DESIGN)

## 1. API 功能
- 构造：`()`、`(int pipeSize)`、`(PipedWriter)`、`(PipedWriter, int)`；`connect(PipedWriter)`。
- 读：`read()`、`read(char[],int,int)`、`ready()`、`skip(long)`；`close()`。

## 2. Java 用户如何使用
- 线程间字符传递：写线程 PipedWriter.write，读线程 PipedReader.read 到 EOF；ready() 判断是否已有数据。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PipedReader.java`。

## 4. 测试用例设计 + 自动化实现（PipedReaderTest.java，纯黑盒）
黑盒场景:
1. 未连接读 → "Pipe not connected"。
2. 单写线程 a-z + 主线程排空 + EOF。
3. read(char[],off,len) 区域填充、未触槽保持 0。
4. ready() 空/有数据/排空三态。
5. 写端 close 交付 EOF。
6. 读端 close 后读抛 "Pipe closed"；写端向已关闭读端写失败。
7. 二次 connect 拒绝。
8. skip 前进 + read 续读。
9. 小管道(1) 在读者排空时往返两字符。
- 健壮性: daemon 线程 + join 超时；无反射白盒。
