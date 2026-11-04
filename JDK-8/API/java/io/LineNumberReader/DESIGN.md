# java.io.LineNumberReader 测试方案 (DESIGN)

## 1. API 功能
- BufferedReader 子类，跟踪行号：`getLineNumber()`/`setLineNumber(int)`；行终止符为 `\n`、`\r`、`\r\n`（`\r` 读为 `\n`，`\r\n` 折叠为一个）。
- 覆写 read()/read(char[])/readLine()/skip()/mark()/reset()。

## 2. Java 用户如何使用
- 逐行读取并定位：`while ((l = lnr.readLine()) != null) { int n = lnr.getLineNumber(); }`。

## 3. 开源代码实际用法调研
- 行号维护 — `jdk8u-dev/jdk/src/share/classes/java/io/LineNumberReader.java`（read/readLine 内 lineNumber++）。

## 4. 测试用例设计 + 自动化实现（LineNumberReaderTest.java，纯黑盒）
黑盒场景:
1. read() 逐字符：`\n`/`\r`/`\r\n` 各计 1 行、`\r` 交付为 `\n`、`\r\n` 折叠；JDK 8 中 read()==-1 不计最后未终止行（与 JDK 9+ 不同）。
2. readLine 剥离终止符、EOF 返回 null、行号推进。
3. read(char[]) 批量读也计数。
4. setLineNumber 任意设值（含负数）后继续计数。
5. mark/reset 同时恢复位置与行号。
6. skip 跨终止符计数。
7. 空输入、read EOF、close 幂等 + 关闭后 readLine 抛 IOException("Stream closed")。
8. 超默认缓冲区长行正确计数。
- 健壮性: 内存 StringReader；无反射白盒。

## 深化追加（契约级）

新增维度（断言 36 → 103）：
- 行界矩阵："L1\nL2\rL3\r\nL4\r\rL5" → readLine 6 行且行号精确。
- bulk read 传递**原始 \r\n**（不翻译成 \n）但行号照计——易错契约。
- 跨缓冲（buffer 4）"ab\r\ncdefgh"：\r\n 分跨 refill 边界只计 1 行（无双计，关键历史坑，实测）。
- setLineNumber 跳变 + 后续 readLine 正确计；getLineNumber 关闭后仍可用（不查 open）。
- skip 语义：先耗当前行剩余、跨行计数；skip(100) 只返回实际 3。
- 构造 "Buffer size <= 0"。

负例矩阵（实测双侧一致）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| skip(-1) | IllegalArgumentException | skip() value is negative（带括号，区别于基类 Reader 的 "skip value is negative"） |
| size<=0 构造 | IllegalArgumentException | Buffer size <= 0 |
| 关闭后 read/skip/readLine | IOException | Stream closed |

**版本差异（实测分叉，分别固化断言）**：EOF 处最后未终止行的行号计数——JDK-8 的 read()/skip() 读到 EOF **不**把尾行计入行号（0/3/3 序列），JDK-27 **计入**（1/4/4 序列）；readLine() 在两版都计尾行。测试按 JDK 分支断言。
