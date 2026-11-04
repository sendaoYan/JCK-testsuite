# java.io.FileReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(File)`、`(String path)`、`(FileDescriptor)`（继承 InputStreamReader 的 read 系列）。
- 读：`read()`、`read(char[])`、`read(char[],int,int)`。
- 注意：JDK 8 无 `FileReader(File, Charset)`（JDK 11+）；字符编码跟随默认 Charset。

## 2. Java 用户如何使用
- `try (FileReader fr = new FileReader(f)) { int c; while ((c = fr.read()) != -1) ... }` 按字符读文本文件。
- 需要固定编码时改用 `new InputStreamReader(new FileInputStream(f), Charset)`。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new FileReader(file)` — `jdk8u-dev/jdk/src/share/classes/java/io/FileReader.java`（薄封装 InputStreamReader）。
- 文本读取样板 — 大量工具代码 `read(char[])` 循环。

## 4. 测试用例设计 + 自动化实现（FileReaderTest.java，纯黑盒）
黑盒场景（ASCII 内容保证默认编码确定性）:
1. FileWriter 写 "hello-file" → FileReader 逐字符 read + read(char[],off,len) 区域 + 拼接断言。
2. String 构造全量 read(char[]) 读回 + EOF。
3. 打开不存在文件 → FileNotFoundException。
- 健壮性: ASCII 内容跨默认编码一致；临时目录 + 递归清理。

## 深化追加（契约级）

新增维度（断言 11 → 55）：
- 构造负例：null File/String → NPE；目录 → FNFE 消息含 "Is a directory" 与路径；缺失文件 → FNFE 含 "No such file or directory" 与文件名（File 与 String 构造都验）。
- FileDescriptor 构造：用 **FileInputStream 的 FD** 读 "hi"+EOF（write-side FD 会 EBADF——实测坑）。
- 关闭矩阵：read()/skip()/ready()/read(char[]) → IOException "Stream closed"；close 幂等；markSupported()==false（继承 InputStreamReader）。
- read 越界：null NPE、负 off IOOBE、越界 len IOOBE、off+len int 溢出 IOOBE、len=0 返回 0；区域读精确 offset 放置（buf[0]==0、buf[1]=='h'）。
- 字节级写回：FileOutputStream 写 ISO-8859-1 字节 + 分块读回拼接；空文件 read()=-1/bulk=-1/ready=false。
- 200 字符混合读：单读+skip(2)+bulk(100)+剩余 bulk(96)+EOF+skip(5)==0。
- 更多语义：ready 随数据/EOF；负 skip IAE "skip value is negative"（继承 ISR）；两个 reader 位置独立；目录 String 构造 FNFE。
- 内容全部 ASCII（JDK-8 无 Charset 构造，非 ASCII 依赖默认 charset 不确定）。

负例矩阵（实测双侧一致）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| null File/String | NullPointerException | 类型级 |
| 打开目录 | FileNotFoundException | Is a directory |
| 打开缺失文件 | FileNotFoundException | No such file or directory |
| 关闭后 4 操作 | IOException | Stream closed |
| skip(负数) | IllegalArgumentException | skip value is negative |
| read(cbuf,off,len) 越界 | IndexOutOfBoundsException | 类型级 |

版本差异：JDK-8 无 (File,Charset)/(String,Charset) 构造（JDK-11+），charset 维度在 JDK-27 侧覆盖。
