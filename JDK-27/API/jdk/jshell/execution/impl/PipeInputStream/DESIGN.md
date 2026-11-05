# jdk.jshell.execution.impl.PipeInputStream（内部类，白盒） 设计案

## 1. API 功能
单字节宽（int[] 缓冲、无符号语义）的单生产者管道流：createOutput() 返回写端；空读阻塞在 wait() 上；close 后读端持续 -1、写端抛 IllegalStateException。

## 2. Java 用户如何使用
Util.forwardExecutionControlAndIO/remoteInputOutput 用它把多路复用通道拆成独立流。

## 3. 开源代码实际用法调研
来源：ConsoleImpl.java 同包 PipeInputStream.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
反射构造 + FIFO 顺序/批量 read/无符号 255/零长 read/越界 IndexOutOfBounds/close→EOF/写后关闭 IllegalStateException("Already closed.")。易错：read() 无数据会永久阻塞——只写后读或关闭后读；available() 只在缓冲非空时非 0。

