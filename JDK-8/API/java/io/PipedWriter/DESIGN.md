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
