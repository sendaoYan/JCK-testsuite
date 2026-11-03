# java.io.InputStreamReader 测试方案 (DESIGN)

## 1. API 功能
- 构造: `InputStreamReader(InputStream)`、`InputStreamReader(InputStream,String)`、`InputStreamReader(InputStream,Charset)`、`InputStreamReader(InputStream,CharsetDecoder)`
- 读: `read()`、`read(char[],int,int)`
- 编码: `getEncoding()`、`ready()`、`close()`

## 2. Java 用户如何使用
- `new InputStreamReader(in, StandardCharsets.UTF_8)` 把字节流按字符集解码为字符流；`getEncoding()` 返回规范编码名。
- 组合链路：UTF-8 字节 → 解码 read → 字符序列；`BufferedReader(new InputStreamReader(...))` 按行读。

## 3. 开源代码实际用法调研（真实片段）
- `new InputStreamReader(resource, StandardCharsets.ISO_8859_1)` — `jdk25u-dev/test/jdk/tools/jpackage/junit/share/jdk.jpackage/jdk/jpackage/internal/OverridableResourceTest.java:104`
- `new InputStreamReader(in, StandardCharsets.UTF_8)` — `jdk25u-dev/test/jdk/tools/jpackage/helpers-test/jdk/jpackage/test/JUnitAdapter.java:76`
- `new InputStreamReader(new ByteArrayInputStream(buf), charset)` — `jdk25u-dev/test/jdk/tools/jpackage/helpers-test/jdk/jpackage/test/ExecutorTest.java:321`

## 4. 测试用例设计 + 自动化实现（指向 InputStreamReaderTest.java 黑盒 `testBlackBox`）
- 正常/组合: 多字节 UTF-8（含非 ASCII）解码 read 为正确字符；read(char[],off,len) 子区间；getEncoding 规范名（实测 "UTF8"）；ready 不抛。
- 边界: EOF read()==-1；空输入。
- 健壮性: 固定 UTF-8 字节；显式 Charset 不依赖默认编码。
- 白盒 `testWhiteBox`: 继承 Reader、getEncoding/ready 存在。
