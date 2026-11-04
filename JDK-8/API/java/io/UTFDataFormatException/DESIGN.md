# java.io.UTFDataFormatException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；表示 modified UTF-8 格式错误。

## 2. Java 用户如何使用
- readUTF/writeUTF 遇到非法编码或超长时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/UTFDataFormatException.java`；DataInput/Output 抛出。

## 4. 测试用例设计 + 自动化实现（UTFDataFormatExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. 非法首字节 0xFF → "malformed"。
3. 非续字节的 2 字节序列 → 抛。
4. 截断 3 字节序列 → 抛。
5. 声明长度超过实际字节 → EOFException（对比）。
6. writeUTF 超 65535 字节 → UTFDataFormatException。
7. 合法 ASCII/2 字节/3 字节/NUL 往返（对比）。
8. 作为 IOException 捕获。
- 健壮性: 确定性字节；无反射白盒。
