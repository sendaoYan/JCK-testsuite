# java.io.StreamCorruptedException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 ObjectStreamException；表示序列化流损坏。

## 2. Java 用户如何使用
- ObjectInputStream 读取坏流时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/StreamCorruptedException.java`；ObjectInputStream 头部/标签校验抛出。

## 4. 测试用例设计 + 自动化实现（StreamCorruptedExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. 无效流头 → "invalid stream header"。
3. 截断头 → StreamCorruptedException 或 EOFException。
4. 合法头后未知标签 → "invalid type code"。
5. 手工破坏载荷 → 值改变或流/解码异常。
6. 未破坏流干净往返（对比）。
7. 作为 ObjectStreamException 捕获。
- 健壮性: 确定性字节；无反射白盒。
