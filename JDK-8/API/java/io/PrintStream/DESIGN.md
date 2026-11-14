# java.io.PrintStream 测试方案 (DESIGN)

## 1. API 功能
- 构造：`(OutputStream, boolean autoFlush, String encoding)`（JDK 8 无 `(..., Charset)` 与 `charset()`，均 JDK 10+）。
- 输出：`print/println`（各类型）、`printf/format`（Locale 重载）、`append`；`flush()`、`checkError()`。

## 2. Java 用户如何使用
- `System.out.printf(Locale.US, "%d%n", n)` 等格式化输出；错误静默记入 checkError 而非抛 IOException。

## 3. 开源代码实际用法调研
- 格式化实现 — `jdk8u-dev/jdk/src/share/classes/java/io/PrintStream.java`（Formatter 委托）。

## 4. 测试用例设计 + 自动化实现（PrintStreamTest.java，纯黑盒）
黑盒场景（显式 "UTF-8" 编码 + Locale.US 保证确定性）:
1. printf 定点小数 + 返回 this；println/print/append 内容；checkError false。
2. format("%05d") → "00042"。
3. UTF-8 println 非 ASCII → 精确字节 + 平台行分隔符。
4. 空 println 仅输出行分隔符。
- 健壮性: 显式 UTF-8 + Locale.US；无反射白盒（JDK 8 无 charset() 方法）。

## 深化追加（契约级，批次 ioG，双侧 JDK-8+JDK-27 同步）

### 新增维度清单（9→104 断言）
1. **吞异常 + checkError 语义（重中之重）**：探针 OutputStream（可注入 write/flush/close 失败）证明：
   - print/println/write(int)/write(byte[],int,int)/write(byte[])/printf 在底层抛 IOException 时**全部不抛出**，返回后 `checkError()==true`；
   - error **粘性**：底层恢复后再次成功写入（"kept" 到达底层），checkError 仍 true；
   - `flush()` 不清 error；底层 flush 抛 IOException 同样被吞（置 error）；
   - closeError 路径：底层 close() 抛 → 被吞、底层仍被标记 closed、checkError true；
   - close 后 print/println/write/flush 全被吞（ensureOpen IOException → trouble），不抛任何异常；
   - close 幂等（clean 双 close 无异常、checkError false）；try-with-resources 正常关闭底层并下沉内容；
   - `checkError()` 本身会 flush 一个未关闭的健康底层（flushCount +1）。
2. **println 全重载精确字节**（UTF-8 + System.lineSeparator()）：boolean×2/char/int×4(含 MIN_VALUE)/long×2(含 MIN_VALUE)/float 1.5f/double 2.0/0.0/-0.0/NaN/±Infinity(float+double)/char[]/String null/Object null/void/custom toString —— 整串一次精确比对。
3. **print 全重载**：null String/Object → "null"；float/double NaN/±Inf → "NaN"/"Infinity"/"-Infinity"（String.valueOf，不受 locale 影响，println(1234.56)="1234.56" 实证）；boolean 两态；'中' UTF-8 往返。
4. **write 族**：write(int) 只保留低 8 位（0x141→0x41、-1→0xFF）且绕过编码器（raw 字节路径）；write(byte[],off,len) 越界 5 组 → IndexOutOfBoundsException（不被吞、不置 error）；write(byte[]) 为继承自 FilterOutputStream 的公开 API（null → NPE、失败被吞）。
5. **printf/format**：Locale.ROOT 下 %d/%s/%f/%x/%05d/%b/%c/%3s 精确串；三种调用返回 this；**非法格式串不被吞**：%z→UnknownFormatConversionException、%d+String→IllegalFormatConversionException（IllegalFormatException 族照抛，且不置 error——吞的只有 IOException）；null format → NPE。
6. **append 三重载**：均返回 this；append((CharSequence)null) 写 "null"（实测，非 NPE）；append(null,0,2) 写 "nu"（null 被替换为 "null" 再取子序列，实测）；append(char) 走编码器。
7. **autoFlush 探针计数**：true 时 write('a')=0 次 flush、write('\n')=+1（规范）、write(byte[])=+1、println 增加；false 时全 0 直到显式 flush()==1；(OutputStream) 单参构造默认 false。**注意**：print(String) 在 autoFlush=true 时也会经编码器回调触发底层 flush（实现行为，非规范承诺，双侧一致，故不断言）。
8. **构造矩阵**：(File)/(String)/(File,String)/(String,String) 写读精确字节（UTF-16BE 原始字节 00 63...）；重开截断为 0；**charset 先于文件打开被校验**——(File,"Bogus") 抛 UEE 且文件不被创建（实证）。
9. **负例矩阵（实测精确异常/消息）**：

| 操作 | 异常 | 双侧消息子串 |
|---|---|---|
| new PrintStream((OutputStream)null) | NullPointerException | "Null output stream" |
| new PrintStream(os,b,(String)null) | NullPointerException | "charsetName" |
| new PrintStream(os,b,"Bogus") | UnsupportedEncodingException | "Bogus" |
| new PrintStream((String)null) / ((File)null) | NullPointerException | — |
| new PrintStream(缺父目录 File/String) | FileNotFoundException | 平台 OS 原因文本 |
| write(b,-1,2)/(0,4)/(4,0)/(2,-1)/(4,-1) | IndexOutOfBoundsException | JDK-8 消息为 null，JDK-27 为 "Range [...]"（双侧只断类型） |
| write((byte[])null[,..]) | NullPointerException | — |
| print/println((char[])null) | NullPointerException | — |
| printf null format | NullPointerException | — |

### 版本差异（本侧为基线）
JDK-8 无 charset()/writeBytes(byte[])/(OutputStream,boolean,Charset)/(File,Charset)/(String,Charset) —— 测试以 getDeclaredMethod/getConstructor 抛 NoSuchMethodException 锁定基线（3 断言）。write(byte[]) 虽为继承方法但双侧可直接调用，行为一致（失败被吞、null NPE）。
