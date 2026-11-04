# java.io.FileWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(File)`、`(File, boolean append)`、`(String path)`、`(String, boolean append)`、`(FileDescriptor)`。
- 写：`write(String)`、`write(String,int,int)`、`write(char[],int,int)`、`append(char)`、`append(CharSequence)`。
- 语义：默认 append=false 截断；append=true 追加；`flush()`/`close()` 落盘。
- 注意：JDK 8 无 `FileWriter(File, Charset)`（JDK 11+）。

## 2. Java 用户如何使用
- `try (FileWriter fw = new FileWriter(f)) { fw.write(text); }` 写文本文件；`(f, true)` 追加日志。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new FileWriter(file)` — `jdk8u-dev/jdk/src/share/classes/java/io/FileWriter.java`（薄封装 OutputStreamWriter）。
- 追加写 — 大量工具代码 `new FileWriter(f, true)`。

## 4. 测试用例设计 + 自动化实现（FileWriterTest.java，纯黑盒）
黑盒场景（ASCII 内容保证默认编码确定性）:
1. write("ab") → 长度 2；append(char) 追加 → "abcd" 读回。
2. 默认模式截断 → 长度 2 + "xy" 读回。
3. write(String,off,len) 区域写 → "bcd"。
4. String 构造 + append + flush → "bcdz"。
5. write(char[],off,len) 区域写 → "QR"。
- 健壮性: ASCII 内容跨默认编码一致；临时目录 + 递归清理。
