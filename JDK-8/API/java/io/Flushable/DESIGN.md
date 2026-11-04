# java.io.Flushable 测试方案 (DESIGN)

## 1. API 功能
- 声明 `flush() throws IOException`：强制缓冲输出落底。
- OutputStream 与 Writer 皆实现；无缓冲实现 flush 为无操作；close 通常也 flush。

## 2. Java 用户如何使用
- 写出后 `flush()` 确保数据到达底层 sink；BufferedOutputStream/Writer 依赖它排空缓冲。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/Flushable.java`。

## 4. 测试用例设计 + 自动化实现（FlushableTest.java，纯黑盒）
黑盒场景:
1. lambda 实现多次 flush 计数。
2. BufferedOutputStream 未 flush 前字节不达底层 Recorder；flush 后到达。
3. flush 失败 IOException 经接口传播。
4. 无缓冲流（ByteArrayOutputStream）flush 无害无操作。
5. close 排空缓冲（BufferedOutputStream）。
6. flush 可重复，每次都被计数实现观察到。
- 健壮性: 确定性 Recorder sink；无反射白盒。
