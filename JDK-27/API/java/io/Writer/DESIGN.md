# java.io.Writer 测试方案 (DESIGN)

## 1. API 功能
- 抽象 `write(char[],int,int)`/`flush`/`close`；默认 `write(int)`/`write(String)`/`write(String,off,len)`/`append(char/CharSequence/CharSequence,int,int)`（返回 this）；protected lock；静态 `nullWriter()`。

## 2. Java 用户如何使用
- 自定义字符汇实现三参 write+flush+close；append 链式调用；nullWriter 占位。

## 3. 开源代码实际用法调研
- 默认实现 — `jdk25u-dev/src/java.base/share/classes/java/io/Writer.java`（write/append 家族、nullWriter 关闭语义）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Writer/`。

## 4. 测试用例设计 + 自动化实现（WriterTest.java）
- 白盒: abstract、Appendable+Closeable+Flushable、三抽象方法、lock、nullWriter。
- 黑盒场景:
  1. 全部 write 形态汇入 write(char[],int,int)（捕获串验证）。
  2. 零长写 no-op。
  3. append 家族内容与返回 this；append(null) 写出 "null"。
  4. flush/close 经基类引用计数。
  5. write(null char[]/String) NPE、越界 IOOBE。
  6. nullWriter 静默接受、close 后抛 "Stream closed"。
  7. StringWriter 经基类引用。
- 健壮性: 全内存。

产出：`WriterTest.java`（白盒 12 + 黑盒 13 断言）。
