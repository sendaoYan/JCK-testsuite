# java.io.LineNumberInputStream 测试方案 (DESIGN)

## 1. API 功能
- 弃用（@Deprecated）的字节侧行号 FilterInputStream：`getLineNumber()`/`setLineNumber(int)`；行终止符 `\n`/`\r`/`\r\n`（`\r` 交付为 `\n`，`\r\n` 折叠为一个字节）。
- read()/read(byte[],int,int)/skip()/available()/mark()/reset()。

## 2. Java 用户如何使用
- 旧式字节流行号追踪（新代码应使用 LineNumberReader）。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/LineNumberInputStream.java`。

## 4. 测试用例设计 + 自动化实现（LineNumberInputStreamTest.java，纯黑盒）
黑盒场景:
1. 单字节读：`\n`/`\r`/`\r\n` 各计 1 行、`\r`→`\n`、`\r\n` 折叠。
2. 批量读：`\r\n` 折叠为 1 字节、两终止符均交付为 `\n`、计 2 行。
3. available 隐藏 `\r\n` 对中第二个字节（pending `\n`）。
4. setLineNumber 移位后继续计数。
5. mark/reset 恢复行号与位置。
6. skip 跨终止符计数。
7. close 透传（ByteArrayInputStream 容忍 close，数据仍可读）。
- 健壮性: 内存 ByteArrayInputStream；无反射白盒。
