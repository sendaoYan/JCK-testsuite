# java.io.CharArrayReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`CharArrayReader(char[])`、`(char[], int offset, int length)`（窗口视图）。
- 读：`read()`、`read(char[], int, int)`、`skip(long)`、`ready()`、`transferTo(Writer)`、`markSupported()`。
- 定位：`mark(int)`、`reset()`（回到 mark；未 mark 回构造起点）。
- `close()` 后 `read()` 抛 IOException。

## 2. Java 用户如何使用
- 内存字符源做解析：`new CharArrayReader("a,b".toCharArray())` 配 StreamTokenizer/手写扫描。
- 偏移窗口只暴露子串；`skip` 后续读；`mark/reset` 回退已消费前缀。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 实现（pos/count/markedPos 游标、close 置 closed 标志、skip 夹取）— `jdk25u-dev/src/java.base/share/classes/java/io/CharArrayReader.java`。
- 窗口构造 + reset 回 mark 用例 — `jdk25u-dev/test/jdk/java/io/CharArrayReader/`（存在性以仓库为准）。

## 4. 测试用例设计 + 自动化实现（CharArrayReaderTest.testBlackBox）
1. 顺序读全部字符 + EOF(-1)；bulk read 区域填充与未触碰槽位。
2. offset/length 窗口只暴露 [off,off+len)。
3. `skip(n)` 返回实际跳过（越界夹紧）、`ready()` 恒真。
4. markSupported()==true；mark→消费→reset 回退；未 mark 直接 reset 回起点。
5. close→read 抛 IOException、close 幂等；空数组即 EOF。
6. transferTo 搬运全部内容。
- 健壮性: 纯内存、固定数据、无线程无临时文件。

产出：`CharArrayReaderTest.java`（白盒 + 黑盒共 39 断言）。
