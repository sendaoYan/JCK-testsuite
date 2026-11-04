# java.io.FileOutputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `FileOutputStream(String)`、`FileOutputStream(String,boolean)`、`FileOutputStream(File)`、`FileOutputStream(File,boolean)`、`FileOutputStream(FileDescriptor)`
- 写: `write(int)`、`write(byte[])`、`write(byte[],int,int)`
- 底层访问: `getFD()`、`getChannel()`、`close()`

## 2. Java 用户如何使用
- `try (FileOutputStream fos = new FileOutputStream(file))` 覆盖写；`new FileOutputStream(file, true)` 追加写。
- 组合链路：写 "ab" → 追加 "cd" → `FileInputStream` 读回 "abcd"；`write(byte[],off,len)` 只写子区间。

## 3. 开源代码实际用法调研（真实片段）
- `new JarOutputStream(new FileOutputStream(tempFile.toFile()))` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:137`
- `try (FileOutputStream fout = new FileOutputStream(cacerts.toFile())) {` — `jdk25u-dev/test/jdk/tools/jlink/runtimeImage/UpgradeableFileCacertsTest.java:138`
- `try (FileOutputStream fos = new FileOutputStream(jar.toFile());` — `jdk25u-dev/test/jdk/tools/jmod/JmodNegativeTest.java:264`

## 4. 测试用例设计 + 自动化实现（指向 FileOutputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: 覆盖写 3 字节 → 追加 2 字节 → 读回 [1,2,3,4,5]；默认模式截断；`write(byte[],off,len)` 只写子区间；getFD/getChannel 非空。
- 边界: `write(int)` 低 8 位语义（配合长度断言）。
- 健壮性: 独立临时目录 + finally 清理；固定字节。
- 白盒 `testWhiteBox`: 继承 OutputStream、write(int)/getFD/getChannel 存在。
