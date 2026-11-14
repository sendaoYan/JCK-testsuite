# java.io.PrintWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造: `PrintWriter(Writer)`、`PrintWriter(Writer,boolean)`、`PrintWriter(OutputStream)`、`PrintWriter(OutputStream,boolean)`、`PrintWriter(String)`、`PrintWriter(File)`、`PrintWriter(Writer,boolean)` + Charset 变体
- 写: `write(int)`、`write(char[],int,int)`、`write(String,int,int)`
- 打印: `print(...)`、`println(...)`、`printf(String,Object...)`、`format(String,Object...)`
- 追加: `append(char)`、`append(CharSequence)`、`append(CharSequence,int,int)`
- 控制: `checkError()`、`setError()`、`flush()`、`close()`、`autoFlush`

## 2. Java 用户如何使用
- `new PrintWriter(writer)` 把格式化文本输出到 Writer；`printf/format` 用 `String.format` 规则；`println` 输出行分隔符。
- 组合链路：`PrintWriter(StringWriter)` → print/println/printf → `checkError()`==false → StringWriter.toString() 精确匹配（行分隔符用 `System.lineSeparator()`）。

## 3. 开源代码实际用法调研（真实片段）
- `new PrintWriter(writer)` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:563`
- `PrintWriter pw = new PrintWriter(writer);` — `jdk25u-dev/test/jdk/tools/jlink/bindservices/BindServices.java:207`
- `PrintWriter pw = new PrintWriter(writer);` — `jdk25u-dev/test/jdk/tools/jlink/bindservices/SuggestProviders.java:312`

## 4. 测试用例设计 + 自动化实现（指向 PrintWriterTest.java 黑盒 `testBlackBox`）
- 正常/组合: print(String/int/char/boolean)、println、printf("%s-%d")、format、append → StringWriter.toString() 精确（换行按 `System.lineSeparator()`）；checkError()==false；write(char[],off,len)。
- 边界: 空 print；printf 格式化数值/字符串。
- 健壮性: 行分隔符统一用 `System.lineSeparator()` 跨平台；内存流目标；无全局副作用。
- 白盒 `testWhiteBox`: 继承 Writer、printf/format/checkError/append/setError 存在。

## 深化追加（契约级，批次 ioG，双侧 JDK-8+JDK-27 同步）

### 新增维度清单（10→121 断言；与 JDK-8 侧同矩阵 + JDK-27 特有）
1. **吞异常 + checkError 语义（重中之重）**：探针 Writer 证明 print/println/write 全五型/append/printf 失败全被吞置 checkError；error 粘性；flush 不清 error；flush 失败被吞；closeError 路径；close 后全操作被吞；close 失败后再 close 重试再吞；clean 双 close 幂等；twr；checkError() flush 健康底层（+1）。
2. **println 全重载精确文本**：同 PrintStream 矩阵（含 MIN_VALUE/0.0/-0.0/NaN/±Inf/null/void/custom toString）。
3. **null 不对称矩阵（双侧实测）**：print((String)null)/(Object)null → "null"；print/println((char[])null) → NPE；write((String)null)/write((char[])null) → NPE。
4. **write 族**：write(int) 低 16 位（0x10041→'A'）；off/len 语义；混用顺序 "ABDE70G"+SEP。
5. **autoFlush 确定性计数**：write=0、print=0、println=+1、printf=+2、format=+3（精确）；print("with\nnewline") 不 flush；false 全 0 直到显式 flush；(Writer)/(OutputStream) 默认 false。
6. **printf/format**：Locale.ROOT 精确 + 返回 this；%z→UnknownFormatConversionException、%d+String→IllegalFormatConversionException（照抛、不置 error）；null format NPE。
7. **append**：返回 this；append(null)→"null"；append(null,0,2)→"nu"（实测）；失败被吞。
8. **OutputStream 包装缓冲**：print 后底层 0 字节，flush 后下沉。
9. **构造矩阵（String 名 + Charset 双轨）**：(File,String)/(String,String) 精确内容 + UTF-16BE 原始字节；截断；(File,"Bogus") UEE 且文件未创建。
10. **JDK-27 特有（javap 复核后直测）**：`(OutputStream,boolean,Charset)` 编码验证（'中'→E4 B8 AD）；`(File,Charset)`/`(String,Charset)` UTF-16BE 精确字节（00 70 00 31 00 36）；(OS,b,(Charset)null) → NPE "charset"；三个 Charset 构造以 getConstructor 白盒断定存在。
11. **负例矩阵**：(Writer)/(OutputStream)/(String)/(File) null → NPE；(File,null csn) → NPE；缺父目录 → FNFE 平台 OS 原因文本（随 OS/locale 本地化）；(file,"Bogus") → UEE "Bogus"（文件未创建）；write(String,0,4)/(-1,1) → **IndexOutOfBoundsException（JDK-27 实测类型；JDK-8 为 StringIndexOutOfBoundsException 子类）**；write(char[],3,0)/(-1,1) → IOOBE；char[]/String null → NPE。

### 版本差异（相对 JDK-8 基线）
新增 (OutputStream,boolean,Charset)/(File,Charset)/(String,Charset)（JDK 10+）；write(String,off,len) 越界异常由 StringIndexOutOfBoundsException 收敛为 IndexOutOfBoundsException；NPE "charset"（Charset 路径）。JDK-8 侧以 getConstructor NoSuchMethodException 锁定 4 个缺席构造（含 OutputStream 路径在 JDK-8 完全无 csn 版本这一事实）。
