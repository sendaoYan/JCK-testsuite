# java.io.BufferedReader 测试方案 (DESIGN)

## 1. API 功能
- 构造: `BufferedReader(Reader)`、`BufferedReader(Reader,int)`
- 读: `read()`、`read(char[],int,int)`
- 行: `readLine()`、`lines()`（Stream<String>）
- 定位: `markSupported()`、`mark(int)`、`reset()`、`skip(long)`、`ready()`
- 传输: `transferTo(Writer)`（JDK 10+）

## 2. Java 用户如何使用
- `new BufferedReader(new InputStreamReader(...))` 按行读取文本；`readLine()` 读到 null 表示 EOF；`lines()` 惰性流式处理。
- 组合链路：`StringReader` 多行 → `readLine` 逐行 → `mark/reset` 回看 → `transferTo` 拷到 Writer。

## 3. 开源代码实际用法调研（真实片段）
- `new BufferedReader(new InputStreamReader(in))` — `jdk25u-dev/test/jdk/tools/jpackage/helpers/jdk/jpackage/test/Executor.java:512`
- `new BufferedReader(new StringReader(s))` — `jdk25u-dev/test/jdk/tools/jar/modularJar/Basic.java:138`
- `new BufferedReader(reader)) {` — `jdk25u-dev/test/jdk/tools/jpackage/helpers-test/jdk/jpackage/test/JUnitAdapter.java:77`
- Web 资料（JDK 教程/社区，readLine 惯用法）: Oracle Java Tutorials `https://docs.oracle.com/javase/tutorial/essential/io/buffers.html`；Baeldung `https://www.baeldung.com/java-buffered-reader`；GeeksforGeeks `https://www.geeksforgeeks.org/java-io-bufferedreader-class-java/`

## 4. 测试用例设计 + 自动化实现（指向 BufferedReaderTest.java 黑盒 `testBlackBox`）
- 正常/组合: 多行输入 readLine 逐行等值 → null 结束；lines().toList() 与预期行序一致；mark/reset 重放一行；skip 跳过字符；transferTo 拷贝到 StringWriter；ready 不抛。
- 边界: 空输入首行 null；行不含终止符（readLine 去掉 \r\n）。
- 健壮性: 固定文本输入（含显式 \n）；lines() 用 `toList()`（保序、JDK16+）避免并行流顺序问题；无墙钟/随机/全局状态。
- 白盒 `testWhiteBox`: 继承 Reader、readLine/lines/mark/reset/transferTo 存在。
