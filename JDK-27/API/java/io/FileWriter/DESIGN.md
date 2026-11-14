# java.io.FileWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造: `FileWriter(String)`、`FileWriter(String,boolean)`、`FileWriter(File)`、`FileWriter(File,boolean)`、`FileWriter(FileDescriptor)`、`FileWriter(String,Charset)`、`FileWriter(File,Charset)`、`FileWriter(String,Charset,boolean)`、`FileWriter(File,Charset,boolean)`
- 继承自 OutputStreamWriter: `write(int)`、`write(char[],int,int)`、`write(String,int,int)`、`getEncoding()`、`flush()`、`close()`
- 继承自 Writer: `write(char[])`、`write(String)`、`append(...)` 等

## 2. Java 用户如何使用
- `try (FileWriter writer = new FileWriter(file))` 覆盖写；`new FileWriter(file, true)` 追加写；`new FileWriter(file, StandardCharsets.UTF_8)` 显式编码。
- 组合链路：写字符串/字符数组 → flush → `FileReader` 读回等值；append 链式追加。

## 3. 开源代码实际用法调研（真实片段）
- `new FileWriter(f)` — `jdk25u-dev/test/jdk/java/io/BufferedReader/ReadLineSync.java:113`
- `new FileWriter(tempFile)` — `jdk25u-dev/test/jdk/java/io/FileDescriptor/Sharing.java:64`
- `new FileWriter(file.getPath(), charset)` — `jdk25u-dev/test/jdk/java/io/FileWriter/ConstructorTest.java:139`

## 4. 测试用例设计 + 自动化实现（指向 FileWriterTest.java 黑盒 `testBlackBox`）
- 正常/组合: 覆盖写 3 字符 → 追加 2 字符 → 读回 5 字符精确；write(String,int,int)/write(char[],off,len) 子区间；append 链式追加；getEncoding 规范名。
- 边界: 默认覆盖截断旧内容；空写不新增。
- 健壮性: 独立临时目录 + finally 清理；显式 UTF-8 Charset。
- 白盒 `testWhiteBox`: 继承 OutputStreamWriter、write/append/flush/close 存在。

## 深化追加（契约级）

新增维度（断言 8 → 67）：与 JDK-8 侧同套（构造负例〔null ×4 形式、缺失父目录 FNFE、目录 FNFE 含 append〕、FileDescriptor 构造、关闭矩阵 "Stream closed"、truncate vs append 全组合、write 越界、write(int) 低 16 位、flush 可见性、逐字符/500 字符大写、append null→"null" 与 "de" 区间、4 写形式一致、继承 getEncoding、重开语义 + no-op 矩阵）。

JDK-27 专属：testCharsetCtors——4 个 Charset 构造：
- (File,UTF_16BE) 写 "ABé" → 6 字节无 BOM，读回精确；
- (String,UTF_16BE) append(CharSequence) 同编码；
- (File,UTF_16BE,true) 追加 2+2 字节；
- **null Charset 4 形式全 NPE 且消息含 "charset"**；
- (File,UTF_8) "héllo 世界" 精确字节长度；
- 继承 getEncoding()=="UnicodeBigUnmarked"。

负例矩阵（实测）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| null Charset（4 形式） | NullPointerException | charset（JDK-27 helpful 消息） |
| null File/String | NullPointerException | 类型级 |
| 缺失父目录 | FileNotFoundException | 平台 OS 原因文本 |
| 打开目录 | FileNotFoundException | 平台 OS 原因文本 |
| 关闭后 write/flush | IOException | Stream closed |
| write("abc",1,5) | StringIndexOutOfBoundsException | 类型级 |

版本差异与实测副作用：**null-Charset 构造先打开/截断目标文件再抛 NPE**（构造链顺序），null 测试一律用一次性文件并断言截断副作用；JDK-8 侧无 Charset 构造。
