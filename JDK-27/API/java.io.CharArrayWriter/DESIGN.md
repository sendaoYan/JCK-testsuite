# java.io.CharArrayWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`()`、`(int initialSize)`。
- 写：`write(int)`、`write(char[])`、`write(char[],off,len)`、`write(String)`、`write(String,off,len)`、`append(CharSequence/char)`。
- 组合：`writeTo(Writer)`（把缓冲注入另一 Writer）、`reset()`（清空复用）。
- `toCharArray()`、`toString()`、`flush()`（无操作）、`close()`（无操作，之后仍可写）。

## 2. Java 用户如何使用
- 无上限内存字符累积器：`toString()` 直接取内容；`writeTo(out)` 免拷贝转发给文件/网络 Writer；`reset()` 循环复用缓冲。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 实现（protected buf/count、扩容 Arrays.copyOf、writeTo 同步转发、close 无操作）— `jdk25u-dev/src/java.base/share/classes/java/io/CharArrayWriter.java`。
- append/CharSequence 语义（toString 视图写入）— 同文件 append 系列。

## 4. 测试用例设计 + 自动化实现（CharArrayWriterTest.testBlackBox）
1. 各 write 形态顺序累积；append(CharSequence) 等价 write(csq.toString())。
2. 初始容量扩容：写超 initialSize 后内容完整。
3. writeTo 把内容注入 StringWriter，源不变。
4. reset 清空后再写只含新内容；toCharArray/toString 一致。
5. close/flush 后继续写成功（规范：二者均为无操作）。
- 健壮性: 纯内存、无全局副作用。

产出：`CharArrayWriterTest.java`（白盒 + 黑盒共 31 断言）。
