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
