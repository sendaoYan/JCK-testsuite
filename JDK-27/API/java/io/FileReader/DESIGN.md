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
