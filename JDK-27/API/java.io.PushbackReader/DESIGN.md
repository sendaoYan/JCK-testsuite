# java.io.PushbackReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(Reader)`（回退 1 字符）、`(Reader, int size)`。
- `unread(int)`、`unread(char[])`、`unread(char[],off,len)`；read 系列（先出回退区）；`ready()`、`skip`、`markSupported()==false`、`close`。

## 2. Java 用户如何使用
- 字符级扫描回退：超前读一个字符判 token 边界，多余字符 unread 回退（FilterReader 的官方实用子类）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 回退缓冲与 "Pushback buffer overflow" — `jdk25u-dev/src/java.base/share/classes/java/io/PushbackReader.java`。
- unread 后 read 次序（缓冲下标递减出栈）— 同文件 ensureOpen/unread。

## 4. 测试用例设计 + 自动化实现（PushbackReaderTest.testBlackBox）
1. read→unread(char)→read 同字符；unread("bc") 后 read 依次 'b','c'（LIFO 回退区）。
2. size-3 构造多字符回退；超容 unread 抛 IOException("Pushback buffer overflow")。
3. ready() 跟随底层 StringReader 恒 true；skip 先耗尽回退区。
4. markSupported()==false；close 后 read 抛 "Stream closed"。
- 健壮性: 纯内存。

产出：`PushbackReaderTest.java`（白盒 + 黑盒共 27 断言）。
