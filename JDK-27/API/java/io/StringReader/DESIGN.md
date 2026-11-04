# java.io.StringReader 测试方案 (DESIGN)

## 1. API 功能
- 构造: `StringReader(String)`
- 读: `read()`、`read(char[],int,int)`
- 定位: `skip(long)`、`markSupported()`、`mark(int)`、`reset()`、`ready()`
- 传输: `transferTo(Writer)`（JDK 10+）
- 关闭: `close()`

## 2. Java 用户如何使用
- `new StringReader(s)` 把字符串当字符流读；`read()` 逐字符、`read(char[],off,len)` 批量。
- 组合链路：字符串 → read 到 EOF → mark/reset 重放 → transferTo 拷到 StringWriter；close 后 read 抛 IOException。

## 3. 开源代码实际用法调研（真实片段）
- `new StringReader(bufferAsString.orElse(""))` — `jdk25u-dev/test/jdk/tools/jpackage/helpers/jdk/jpackage/test/Executor.java:539`
- `new BufferedReader(new StringReader(s))` — `jdk25u-dev/test/jdk/tools/jar/modularJar/Basic.java:138`
- `new StringReader(xmlToString(xml))` — `jdk25u-dev/test/jdk/tools/jpackage/junit/share/jdk.jpackage/jdk/jpackage/internal/util/PListReaderTest.java:480`

## 4. 测试用例设计 + 自动化实现（指向 StringReaderTest.java 黑盒 `testBlackBox`）
- 正常/组合: read() 逐字符到 EOF==-1；read(char[],off,len) 子区间；mark/reset 重放；skip 跳过；transferTo 拷到 StringWriter；ready 不抛。
- 边界/异常: close 后 `read()` 抛 `IOException`（JDK 实际行为，非 no-op，见文件内 close 修复）。
- 健壮性: 固定字符串输入；无墙钟/随机/全局状态。
- 白盒 `testWhiteBox`: 继承 Reader、markSupported/ready/transferTo 存在。
