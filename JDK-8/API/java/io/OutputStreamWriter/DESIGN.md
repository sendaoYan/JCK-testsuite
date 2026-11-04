# java.io.OutputStreamWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(OutputStream)`、`(OutputStream, String charsetName)`、`(OutputStream, Charset)`。
- 写：`write(int)`、`write(String)`、`write(String,int,int)`、`write(char[],int,int)`、`append(char)`、`append(CharSequence)`、`flush()`。
- 编码：`getEncoding()` 返回所用字符集历史名。

## 2. Java 用户如何使用
- `new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)` 以显式编码写文本；`flush()` 前字节在缓冲中。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new OutputStreamWriter(out, StandardCharsets.UTF_8)` — `jdk8u-dev/jdk/src/share/classes/java/io/OutputStreamWriter.java`（StreamEncoder 包装）。
- 官方样板 — `jdk8u-dev/jdk/test/java/io/`（编码往返用例）。

## 4. 测试用例设计 + 自动化实现（OutputStreamWriterTest.java，纯黑盒）
黑盒场景:
1. write("Aé中") + flush → 与 `String.getBytes(UTF_8)` 精确字节一致；getEncoding 非空。
2. write(char[],off,len) + append(char) → "yz!"。
3. CharsetName 构造写 "héllo 世界" → InputStreamReader(Charset) 读回一致。
4. append(char) 链式 "abc" + flush。
- 健壮性: 显式 UTF-8，确定性；内存流。

## 深化追加（契约级）

新增维度（断言 6 → 72）：
- getEncoding 历史名矩阵（UTF8/ISO8859_1/ASCII/UnicodeBigUnmarked），String 与 Charset 构造一致。
- 构造负例：未知名 UEE 含 "Bogus"；null 名/null Charset NPE；单参构造可用（ASCII 内容确定）。
- **精确字节编码矩阵**：UTF-8 "Aé中"→{41,C3,A9,E4,B8,AD}；ISO-8859-1 é→E9、不可映射中→3F('?')；US-ASCII é→3F；UTF-16BE 无 BOM {00,41,00,42,00,E9}；空串 0 字节。
- **内部缓冲语义（CountingStream 探针实测）**：单字符写后底层 writes==0，flush 才下沉——Writer 的缓冲不在 OutputStream 层；字符串写同理。
- flush/close 传播计数：2 次 flush→2 次底层 flush；close 关底层恰 1 次。
- 关闭矩阵（write(int)/write(char[])/write(String)/flush）全 "Stream closed"；关闭后 getEncoding()==null。
- write 越界：null NPE（char[]/String）、off+len 溢出 IOOBE、write("abc",1,5) StringIndexOOB、len=0 no-op；write(int) 取低 16 位（0x100E9→字节 E9）。
- append 契约：链式返回 this；append(null)→字面 "null"；append(null,0,2)→"nu"；全序列 "cABnullnuXYZYZ"。
- 往返矩阵（4 charset × 多文字含 😀）；单字符编码矩阵；1000 字符大写；write(String,off,len)/write(char[],off,len) 区域矩阵。

负例矩阵（实测双侧一致）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| 未知 charset 名 | UnsupportedEncodingException | 消息含 "Bogus" |
| null 名 / null Charset | NullPointerException | 类型级 |
| 关闭后 write/flush | IOException | Stream closed |
| write("abc",1,5) | StringIndexOutOfBoundsException | 类型级 |
| write(cbuf,off,len) 越界 | IndexOutOfBoundsException | 类型级 |

版本差异：JDK-8 无 append(CharSequence) 覆写（继承 Writer 默认）；JDK-27 覆写 2 个 append 重载（见 JDK-27 侧）。
