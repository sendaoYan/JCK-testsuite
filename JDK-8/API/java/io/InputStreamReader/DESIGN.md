# java.io.InputStreamReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(InputStream)`、`(InputStream, String charsetName)`、`(InputStream, Charset)`。
- 读：`read()`、`read(char[])`、`read(char[],int,int)`（继承 Reader）。
- 编码：`getEncoding()` 返回所用字符集历史名。

## 2. Java 用户如何使用
- `new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)` 以显式编码读文本（多字节/增补字符安全）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new InputStreamReader(in, StandardCharsets.UTF_8)` — `jdk8u-dev/jdk/src/share/classes/java/io/InputStreamReader.java`（StreamDecoder 包装）。
- 官方样板 — `jdk8u-dev/jdk/test/java/io/`（编码往返用例）。

## 4. 测试用例设计 + 自动化实现（InputStreamReaderTest.java，纯黑盒）
黑盒场景:
1. OutputStreamWriter(UTF-8) 写 "héllo 世界 😀" → 字节 → InputStreamReader(UTF-8) read(char[],off,len) 分批读回全文一致；EOF。
2. CharsetName 构造单字符读 "abc"。
3. Charset 构造 read(char[]) 逐块读 "xy" + EOF。
- 健壮性: 显式 UTF-8 编码，确定性；内存流。

## 深化追加（契约级）

新增维度（断言 8 → 70）：
- getEncoding 历史名矩阵：UTF-8→"UTF8"、ISO-8859-1→"ISO8859_1"、US-ASCII→"ASCII"、UTF-16BE→"UnicodeBigUnmarked"；String 名与 Charset 对象构造一致。
- 构造负例：未知名 → UnsupportedEncodingException 消息含 "Bogus"；null 名/null Charset → NPE。
- malformed 替换：孤立 0x80→U+FFFD；截断三字节 E4B8→U+FFFD；C3 28→U+FFFD+'('；拼接 "a�b" 精确。
- 解码往返矩阵：4 charset ×（ASCII/拉丁/CJK/增补 😀/空串）经 OutputStreamWriter→InputStreamReader 全往返。
- UTF-16BE 显式字节：{0,65,0,66}→"AB"；3 字节奇数长度 → 'Ø'+U+FFFD（长 2，实测）。
- ready 语义 true/false/EOF false；skip 按字符不按字节（"héllo" skip(2)→"llo"）。
- 增补字符单 read 返回代理对 0xD83D/0xDE00。
- 关闭探针 ProbeInputStream（确认底层流被 close）；close 后 getEncoding()==null；**关闭流上 skip(-1) 仍抛 IAE（参数校验先于 open 校验，实测）**。
- read 越界：null NPE、off/len IOOBE、off+len int 溢出 IOOBE、len=0 返回 0。

负例矩阵（实测双侧一致）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| 未知 charset 名 | UnsupportedEncodingException | 消息含 charset 名（"Bogus"） |
| null 名 / null Charset | NullPointerException | 类型级 |
| skip(负数)（含已关闭） | IllegalArgumentException | skip value is negative |
| 关闭后 read/ready/skip(正) | IOException | Stream closed |
| read(cbuf,off,len) 越界 | IndexOutOfBoundsException | 类型级 |

版本差异：JDK-8 无 read(CharBuffer) 覆写（继承 Reader 默认实现）；JDK-27 覆写（见 JDK-27 侧）。helpful NPE 消息差异按类型级断言。
