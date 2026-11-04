# java.io.FileInputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `FileInputStream(String)`、`FileInputStream(File)`、`FileInputStream(FileDescriptor)`
- 读: `read()`、`read(byte[])`、`read(byte[],int,int)`、`readAllBytes()`、`readNBytes(int)`
- 定位/传输: `skip(long)`、`available()`、`transferTo(OutputStream)`
- 底层访问: `getFD()`、`getChannel()`、`close()`

## 2. Java 用户如何使用
- `try (FileInputStream fis = new FileInputStream(file)) { ... }` 读文件字节。
- 组合链路：`FileOutputStream` 写文件 → `FileInputStream` 逐步 read/skip/readNBytes → `readAllBytes` 全量 → `transferTo` 拷贝到内存流。

## 3. 开源代码实际用法调研（真实片段）
- `try (FileInputStream fs = new FileInputStream(release)) {` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageValidator.java:148`
- `try (FileInputStream fin = new FileInputStream(cacerts.toFile())) {` — `jdk25u-dev/test/jdk/tools/jlink/runtimeImage/UpgradeableFileCacertsTest.java:125`
- `try (FileInputStream fis = new FileInputStream(inFile);` — `jdk25u-dev/test/jdk/tools/launcher/BigJar.java:91`

## 4. 测试用例设计 + 自动化实现（指向 FileInputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: 写已知 7 字节 → read()/read(byte[],off,len)/skip/readNBytes 顺序读 → readAllBytes 全量 → transferTo 拷贝 → getFD/getChannel 非空。
- 边界: EOF `read()==-1`；打开不存在文件抛 `FileNotFoundException`。
- 健壮性: 独立 `Files.createTempDirectory("jck-fis")` + finally 清理；固定字节序列。
- 白盒 `testWhiteBox`: 继承 InputStream、readAllBytes/readNBytes/transferTo/getChannel 存在。
