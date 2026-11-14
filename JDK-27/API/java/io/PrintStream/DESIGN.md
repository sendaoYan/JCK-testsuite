# java.io.PrintStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `PrintStream(OutputStream)`、`PrintStream(OutputStream,boolean)`、`PrintStream(OutputStream,boolean,Charset)`、`PrintStream(String)`、`PrintStream(File)` 等
- 写: `write(int)`、`write(byte[],int,int)`、`writeBytes(byte[])`
- 打印: `print(...)`、`println(...)`、`printf(...)`、`format(...)`
- 追加: `append(char)`、`append(CharSequence)`、`append(CharSequence,int,int)`
- 控制: `checkError()`、`flush()`、`close()`、`charset()`、`autoFlush`
- 契约: 继承 FilterOutputStream、实现 `Appendable` + `Closeable`

## 2. Java 用户如何使用
- `new PrintStream(baos)` 把字节打印输出到内存流；`println` 输出行分隔符；`printf` 格式化；`charset()` 返回所用字符集。
- 组合链路：print/println/printf/append → flush → `baos.toByteArray()` 按 `charset()` 解码精确匹配。

## 3. 开源代码实际用法调研（真实片段）
- `PrintStream ps = new PrintStream(baos);` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:493`
- `PrintStream ps = new PrintStream(baos);` — `jdk25u-dev/test/jdk/tools/jimage/JImageCliTest.java:76`
- `PrintStream ps = new PrintStream(baos);` — `jdk25u-dev/test/jdk/tools/jmod/JmodNegativeTest.java:553`

## 4. 测试用例设计 + 自动化实现（指向 PrintStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: print(String/int/char/boolean)、println、printf("%s-%d")、format、append、write(byte[],off,len) → 按 `charset()` 解码后精确匹配（换行用 `System.lineSeparator()`）；checkError()==false。
- 边界: 空 print；非 ASCII 经 UTF-8 编解码正确。
- 健壮性: 显式 `StandardCharsets.UTF_8` 构造；行分隔符 `System.lineSeparator()`；内存流目标；无全局副作用。
- 白盒 `testWhiteBox`: 继承 FilterOutputStream、实现 Appendable+Closeable、printf/format/checkError/append/charset 存在。

## 深化追加（契约级，批次 ioG，双侧 JDK-8+JDK-27 同步）

### 新增维度清单（13→128 断言；与 JDK-8 侧同矩阵 + JDK-27 特有）
1. **吞异常 + checkError 语义（重中之重）**：探针 OutputStream 证明 print/println/write(int)/write(byte[],int,int)/write(byte[])/writeBytes/printf 在底层抛 IOException 时全部不抛、checkError 置 true；error 粘性；flush 不清 error；closeError 路径（底层 close 抛 → checkError true）；close 后 print/println/write/writeBytes/flush 全被吞；close 幂等；twr 正常关闭；checkError() flush 健康底层（+1）。
2. **println 全重载精确字节**：boolean×2/char/int×4/long×2/float/double/0.0/-0.0/NaN/±Inf/char[]/String null/Object null/void/custom toString，一次整串比对（UTF-8 + System.lineSeparator()）。
3. **print 全重载**：null String/Object → "null"；NaN/±Infinity 精确；println(double) 走 String.valueOf 不受 locale 影响；'中' UTF-8 往返。
4. **write 族（含 JDK-27 特有）**：write(int) 低 8 位（0x141→0x41）；raw 字节路径；越界 5 组 IOOBE（JDK-27 消息 "Range [off, off + len) out of bounds for length N"，双侧仅断类型）；write(byte[]) **声明 throws IOException 但从不抛**（apiNote 明示，失败置 error）；writeBytes(null) NPE、失败被吞、autoFlush 下精确 +1 次 flush。
5. **printf/format**：Locale.ROOT 精确串（%d/%s/%f/%x/%05d/%b/%c/%3s）；三种调用返回 this；%z→UnknownFormatConversionException、%d+String→IllegalFormatConversionException 照抛不吞、不置 error；null format NPE。
6. **append 三重载**：返回 this；append(null)→"null"；append(null,0,2)→"nu"（实测）；append('中') 走 charset。
7. **autoFlush 探针计数**：write('a')=0、write('\n')=+1、write(byte[])=+1、println 增加；false 全 0 直到显式 flush（print(String) 在 autoFlush=true 时经编码器回调也会触发底层 flush——实现行为，不断言）。
8. **构造矩阵（String 名 + Charset 双轨）**：(File,String)/(String,String) 精确内容与 UTF-16BE 原始字节（00 63 00 73...）；重开截断；charset 先于文件打开校验（(File,"Bogus") UEE 且文件未创建）。
9. **JDK-27 特有成员（javap 复核后直测）**：`charset()` 返回构造 Charset（UTF-8/ISO-8859-1 实测）；`(OutputStream,boolean,Charset)` 编码验证（'中'→E4 B8 AD）；`(File,Charset)`/`(String,Charset)` UTF-16BE 精确字节；三者 + write(byte[])/writeBytes/charset() 以 getDeclaredMethod/getConstructor 白盒断定声明存在。
10. **负例矩阵（实测）**：(OutputStream) null → NPE "Null output stream"；(OS,b,(String)null) → NPE "charsetName"；(OS,b,"Bogus") → UEE "Bogus"；**(OS,b,(Charset)null) → NPE "charset"**（JDK-27 特有路径）；(String)/(File) null → NPE；缺父目录 → FNFE 平台 OS 原因文本（随 OS/locale 本地化）；write 越界 → IOOBE；write/print(byte[]/char[]) null → NPE。

### 版本差异（相对 JDK-8 基线）
新增 charset()/writeBytes(byte[])（JDK 14+）/write(byte[]) 覆写声明/(OutputStream,boolean,Charset)/(File,Charset)/(String,Charset)（JDK 10+）；NPE "charset"（Charset 路径）vs "charsetName"（String 路径）；IOOBE 消息从 null 变为 "Range [...]" 格式；println 的编码器回调 flush 批次计数不同（JDK-8=5 vs JDK-27=4，故两侧都只断言增量不断言精确总数）。
