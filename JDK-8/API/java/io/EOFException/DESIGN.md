# java.io.EOFException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；表示数据流意外结束。

## 2. Java 用户如何使用
- 读 DataInput/RandomAccessFile/ObjectInputStream 原始类型时以 IOException/EOFException 捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/EOFException.java`；`DataInputStream.readFully/readUTF` 等抛出。

## 4. 测试用例设计 + 自动化实现（EOFExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器默认 null / 消息保留。
2. readInt 仅 2 字节 → EOFException。
3. 空流上 readLong/readDouble/readChar/readUTF/readFully 各抛 EOFException；readLine/skipBytes 以 null/0 表示 EOF（对比）。
4. readFully 短流填充前缀后抛 EOFException。
5. RandomAccessFile readByte/readInt 越界抛 EOFException。
6. ObjectInputStream 载荷耗尽后 readInt 抛 EOFException。
7. 原始 InputStream.read 返回 -1（对比）。
- 健壮性: 确定性字节序列；无反射白盒。
