# java.io.PrintWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造：`(Writer)`、`(OutputStream)`、`(OutputStream, boolean)` 等（JDK 8 无 `(OutputStream, boolean, Charset)`，JDK 10+）。
- 输出：`print/println`（各类型）、`printf/format`（Locale 重载）、`append`；`flush()`、`checkError()`。

## 2. Java 用户如何使用
- 字符侧格式化输出（日志/文本），从不抛 IOException（错误记入 checkError）；需显式编码时用 OutputStreamWriter 包装。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PrintWriter.java`。

## 4. 测试用例设计 + 自动化实现（PrintWriterTest.java，纯黑盒）
黑盒场景:
1. StringWriter + printf 定点小数 + 返回 this；print(boolean/long) 内容；checkError false。
2. format("%05d") → "00042"。
3. 显式 UTF-8（OutputStreamWriter 包装）println 非 ASCII → InputStreamReader 读回精确 + 平台行分隔符。
- 健壮性: Locale.US + 显式 UTF-8；无反射白盒。

## 深化追加（契约级，批次 ioG，双侧 JDK-8+JDK-27 同步）

### 新增维度清单（8→110 断言）
1. **吞异常 + checkError 语义（重中之重）**：探针 Writer（可注入 write/flush/close 失败）证明 print/println/write(int)/write(String)/write(char[])/write(char[],int,int)/write(String,int,int)/append/printf 全部不抛 IOException 而置 checkError；error 粘性（恢复后 "kept" 成功写入仍 true）；flush 不清 error；flush 失败被吞；closeError 路径（底层 close 抛 → checkError true、底层已标记 closed）；close 后 println/print/write/flush 全被吞且保持 error；**close 失败后再 close 会重试并再次吞掉**（out 未置 null）；clean 双 close 幂等；twr 关闭底层；checkError() 会 flush 未关闭的健康底层（+1）。
2. **println 全重载精确文本**：与 PrintStream 同矩阵（boolean/char/int/long/float/double/char[]/String/Object/void + MIN_VALUE + 0.0/-0.0/NaN/±Inf + custom toString），StringWriter 一次整串比对；println(double) 走 String.valueOf 不受 locale 影响（"1234.56" 实证）。
3. **print 全重载 + null 不对称矩阵（双侧实测）**：print((String)null)="null"、print((Object)null)="null"；**print((char[])null) 与 println((char[])null) 抛 NPE**（与 print(String null) 不对称）；**write((String)null) 与 write((char[])null) 抛 NPE**（write 不做 null→"null" 替换，PrintWriter 自声明 write(char[])/write(String) 正是为此不复用 Writer 基类）。
4. **write 族**：write(int) 低 16 位（0x10041→'A'）；write(String,off,len)/write(char[],off,len) 内容与偏移；write/print/append 混用顺序精确（"ABDE70G"+SEP）。
5. **autoFlush 确定性计数（与 PrintStream 不同，PrintWriter 无编码器回调路径）**：true 时 write(int)=0、print(String)=0、println=+1、printf=+2、format=+3（精确断言）；print("with\nnewline") 含换行也**不**flush（字符侧语义）；false 时全 0 直到显式 flush；(Writer)/(OutputStream) 默认 false。
6. **printf/format**：Locale.ROOT 精确串 + 三重载返回 this；%z→UnknownFormatConversionException、%d+String→IllegalFormatConversionException（**格式异常照抛不被吞**，也不置 error）；null format → NPE。
7. **append 三重载**：返回 this；append(null)→"null"；append(null,0,2)→"nu"（实测）；失败被吞。
8. **OutputStream 包装**：PrintWriter(OutputStream) 套 BufferedWriter——print 后底层 0 字节，flush() 后才下沉（与 Writer 直写对比）。
9. **构造矩阵**：(File,String)/(String,String) 精确内容+行分隔符；UTF-16BE 原始字节；重开截断；charset 先于文件打开校验（(File,"Bogus") UEE 且文件未创建，实证）。
10. **负例矩阵（实测精确异常/消息）**：

| 操作 | 异常 | 双侧消息子串 |
|---|---|---|
| new PrintWriter((Writer)null[,true]) | NullPointerException | — |
| new PrintWriter((OutputStream)null[,true]) | NullPointerException | — |
| new PrintWriter((String)null)/((File)null) | NullPointerException | — |
| new PrintWriter((File)null,(String)null) | NullPointerException | — |
| new PrintWriter(缺父目录) | FileNotFoundException | 平台 OS 原因文本 |
| new PrintWriter(file,"Bogus") | UnsupportedEncodingException | "Bogus"（文件未创建） |
| write("abc",0,4)/(-1,1) | **JDK-8: StringIndexOutOfBoundsException；JDK-27: IndexOutOfBoundsException**（SIOOBE⊂IOOBE） | — |
| write(char[],3,0)/(-1,1) | IndexOutOfBoundsException | — |
| print/println((char[])null)、write((String)null)、write((char[])null) | NullPointerException | — |

### 版本差异（本侧为基线）
JDK-8 无 (OutputStream,boolean,String)/(OutputStream,boolean,Charset)/(File,Charset)/(String,Charset) 构造（注意 OutputStream 路径在 JDK-8 连 csn 名版本都没有）——以 getConstructor NoSuchMethodException 锁定（4 断言）。write(String,off,len) 越界异常类型双侧不同（见上表），各自断言精确类型。
