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
