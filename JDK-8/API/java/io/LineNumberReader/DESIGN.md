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
