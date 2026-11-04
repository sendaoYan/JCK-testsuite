# java.io.LineNumberReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(Reader)`、`(Reader, int sz)`；继承 BufferedReader 全部读 API（readLine 等）。
- 行语义：`getLineNumber()`/`setLineNumber(int)`；\n、\r、\r\n 都是一行（\r 以 '\n' 投递、\r\n 折叠为单次）。
- mark/reset 连同行号一并回退；skip 跨行计数；EOF 使未终结末行计数。

## 2. Java 用户如何使用
- 带行号的日志/配置逐行处理：readLine + getLineNumber 定位；跨平台换行统一化。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 行计数与 CR/CRLF 折叠实现 — `jdk25u-dev/src/java.base/share/classes/java/io/LineNumberReader.java`。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/LineNumberReader/`。

## 4. 测试用例设计 + 自动化实现（LineNumberReaderTest.testBlackBox）
1. read() 逐字符验证三种终止符都投递 '\n' 且各计 1 行（\r\n 只计一次）。
2. EOF 使未终结末行计数（+1）——探针证实的边界语义。
3. readLine 剥离终止符、EOF 后返回 null；bulk read 同样计数。
4. setLineNumber 任意值（含负数）后续计数延续；mark/reset 回退行号与位置。
5. skip 跨终止符计数；超长行（500 字符超缓冲）跨缓冲计数正确；close 后 readLine 抛 "Stream closed"。
- 健壮性: StringReader 固定文本。

产出：`LineNumberReaderTest.java`（白盒 + 黑盒共 45 断言）。

## 深化追加（契约级）

新增维度（断言 41 → 114）：与 JDK-8 侧同套（行界矩阵、bulk 原始 \r\n 但行号照计、buffer 4 跨界无双计、setLineNumber 跳变、skip 先耗行内/跨行计数、构造 "Buffer size <= 0"）。

负例矩阵（实测双侧一致）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| skip(-1) | IllegalArgumentException | skip() value is negative |
| size<=0 构造 | IllegalArgumentException | Buffer size <= 0 |
| 关闭后 read/skip/readLine | IOException | Stream closed |

**版本差异（实测分叉）**：EOF 尾行计数——JDK-27 的 read()/skip() 到 EOF 把最后未终止行计入行号（1/4/4），JDK-8 不计（0/3/3）；readLine() 两版都计。测试按 JDK 分支断言。
