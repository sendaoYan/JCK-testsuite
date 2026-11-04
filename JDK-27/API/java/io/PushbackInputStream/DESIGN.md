# java.io.PushbackInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(InputStream)`（回退 1 字节）、`(InputStream, int size)`。
- `unread(int)`、`unread(byte[])`、`unread(byte[],off,len)`（LIFO 回退）；`read()`/`read(byte[],int,int)`（先出回退区）；`available()`（含回退区计数）。
- `skip`、`markSupported()==false`（不支持 mark/reset）、`close`。

## 2. Java 用户如何使用
- 词法扫描回退超前读：读一个字节判定类型，不合则 `unread` 回去重新读；解析器/StreamTokenizer 类逻辑。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 回退缓冲实现与 "Push back buffer is full" — `jdk25u-dev/src/java.base/share/classes/java/io/PushbackInputStream.java`。
- markSupported 返回 false 的覆盖 — 同文件。

## 4. 测试用例设计 + 自动化实现（PushbackInputStreamTest.testBlackBox）
1. read→unread→read 得到同一字节（LIFO）；unread 多字节后按后进先出次序吐出。
2. 显式 size-2/size-3 构造下多字节回退；默认 1 字节缓冲第二次 unread 抛 IOException("Push back buffer is full")。
3. available() = 底层 + 已回退计数；skip 先耗尽回退区。
4. markSupported()==false；close 后 read 抛异常。
- 健壮性: BAIS 固定数据。

产出：`PushbackInputStreamTest.java`（白盒 + 黑盒共 31 断言）。
