# java.io.ByteArrayOutputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `ByteArrayOutputStream()`、`ByteArrayOutputStream(int)`
- 写: `write(int)`、`write(byte[],int,int)`、`writeBytes(byte[])`
- 读回/转换: `toByteArray()`、`size()`、`toString()`、`toString(String)`、`toString(Charset)`、`toString(int)`
- 控制/传输: `reset()`、`writeTo(OutputStream)`、`close()`

## 2. Java 用户如何使用
- `new ByteArrayOutputStream()` 累积字节，`toByteArray()` 取回，`toString(StandardCharsets.UTF_8)` 解码文本。
- 组合链路：写入 → `toByteArray` → 回灌 `ByteArrayInputStream` → `readAllBytes` 逐字节比对；`writeTo` 拷到另一输出流。

## 3. 开源代码实际用法调研（真实片段）
- `ByteArrayOutputStream bout = new ByteArrayOutputStream();` — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectStreamClass.java:1600`
- `ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream();` — `jdk25u-dev/src/java.base/share/classes/java/util/zip/snippet-files/Snippets.java:40`
- `baos = new ByteArrayOutputStream();` — `jdk25u-dev/src/java.base/share/classes/java/util/jar/JarVerifier.java:109`

## 4. 测试用例设计 + 自动化实现（指向 ByteArrayOutputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: write(int)/write(byte[],off,len)/writeBytes → size=4 → toByteArray 内容 → toString(Charset)；writeTo 拷贝；reset 清空；小初始容量自动扩容并 roundtrip 10000 字节。
- 边界: `toByteArray()` 返回副本（外部改动不影响内部）；空流 `toByteArray().length==0`；非 ASCII `toString(UTF_8)` 正确。
- 健壮性: 固定 UTF-8、固定输入；无全局副作用。
- 白盒 `testWhiteBox`: 继承 OutputStream、toByteArray/size/reset/toString(Charset)/writeTo 存在。
