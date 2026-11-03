# java.io.BufferedWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造: `BufferedWriter(Writer)`、`BufferedWriter(Writer,int)`
- 写: `write(int)`、`write(char[],int,int)`、`write(String,int,int)`
- 换行: `newLine()`
- 刷新/关闭: `flush()`、`close()`

## 2. Java 用户如何使用
- `new BufferedWriter(new FileWriter(f))` 缓冲写文本；`newLine()` 输出平台换行符（等价 `System.lineSeparator()`）。
- 组合链路：写字符串/字符数组 → `newLine` → flush → 目标 StringWriter 内容一致。

## 3. 开源代码实际用法调研（真实片段）
- `BufferedWriter w = new BufferedWriter(...)` — `jdk25u-dev/test/jdk/java/io/BufferedReader/ReadLineSync.java:112`
- `BufferedWriter bw = new BufferedWriter(w);` — `jdk25u-dev/test/jdk/java/io/BufferedWriter/Cleanup.java:63`
- `new BufferedWriter(new FailingFlushWriter())` — `jdk25u-dev/test/jdk/java/io/etc/FailingFlushAndClose.java:305`

## 4. 测试用例设计 + 自动化实现（指向 BufferedWriterTest.java 黑盒 `testBlackBox`）
- 正常/组合: write(String,int,int)/write(char[],off,len)/write(int) → flush → 目标内容精确；newLine 与 System.lineSeparator 一致；close 后 flush 语义。
- 边界: 空串写 0；小缓冲区容量自动冲刷。
- 健壮性: 换行符用 `System.lineSeparator()` 跨平台；内存流目标；无全局副作用。
- 白盒 `testWhiteBox`: 继承 Writer、newLine/flush/close 存在。
