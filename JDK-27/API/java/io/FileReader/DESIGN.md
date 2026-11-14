# java.io.FileReader 测试方案 (DESIGN)

## 1. API 功能
- 构造: `FileReader(String)`、`FileReader(File)`、`FileReader(FileDescriptor)`、`FileReader(String,Charset)`、`FileReader(File,Charset)`
- 继承自 InputStreamReader: `read()`、`read(char[],int,int)`、`getEncoding()`、`ready()`、`close()`
- 继承自 Reader: `read(char[])`、`skip(long)`、`transferTo(Writer)` 等

## 2. Java 用户如何使用
- `try (FileReader reader = new FileReader(file))` 按默认/指定字符集读文件字符；`new FileReader(file, StandardCharsets.UTF_8)` 显式编码。
- 组合链路：`FileWriter` 写文本文件 → `FileReader` 读回逐字符等值；read(char[],off,len) 子区间。

## 3. 开源代码实际用法调研（真实片段）
- `try (FileReader reader = new FileReader(release)) {` — `jdk25u-dev/test/jdk/tools/jlink/ModuleNamesOrderTest.java:86`
- `try (FileReader reader = new FileReader(release)) {` — `jdk25u-dev/test/jdk/tools/jlink/IntegrationTest.java:211`
- `new FileReader(f)` — `jdk25u-dev/test/jdk/java/io/BufferedReader/ReadLineSync.java:48`

## 4. 测试用例设计 + 自动化实现（指向 FileReaderTest.java 黑盒 `testBlackBox`）
- 正常/组合: 写已知文本文件 → read()/read(char[],off,len) 读回等值；getEncoding 规范名；skip 跳过字符；read 到 EOF==-1。
- 边界: 打开不存在文件抛 `FileNotFoundException`；空文件 EOF。
- 健壮性: 独立临时目录 + finally 清理；显式 UTF-8 Charset 构造。
- 白盒 `testWhiteBox`: 继承 InputStreamReader、read/close 存在。

## 深化追加（契约级）

新增维度（断言 11 → 66）：与 JDK-8 侧同套（构造负例含目录/缺失 FNFE 精确子串、FileInputStream-FD 构造、关闭矩阵 4 操作 "Stream closed"、read 越界含 int 溢出、字节级分块写回、200 字符混合读+skip、ready/独立位置/负 skip）。

JDK-27 专属：testCharsetCtors——(File,Charset)/(String,Charset)：UTF-16BE "ABé" 精确解码；getEncoding()=="UnicodeBigUnmarked"；bulk offset 读；null Charset NPE ×2；(File,Charset) 缺失文件/目录 FNFE 精确子串；UTF-8 "héllo 世界" 多文字解码。

负例矩阵（实测双侧一致）：
| 操作 | 异常 | 精确子串 |
|---|---|---|
| null File/String/null Charset | NullPointerException | 类型级（JDK-27 NPE 带具名消息） |
| 打开目录（含 Charset 构造） | FileNotFoundException | 平台 OS 原因文本 |
| 缺失文件（含 Charset 构造） | FileNotFoundException | 平台 OS 原因文本 |
| 关闭后 4 操作 | IOException | Stream closed |
| skip(负数) | IllegalArgumentException | skip value is negative |
| read(cbuf,off,len) 越界 | IndexOutOfBoundsException | 类型级 |

版本差异：JDK-11+ 增加 2 个 Charset 构造（JDK-8 侧无）；JDK-27 默认 charset UTF-8（JEP 400），显式 Charset 构造规避差异。
