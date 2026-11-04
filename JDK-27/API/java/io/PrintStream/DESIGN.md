# java.io.PrintStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `PrintStream(OutputStream)`、`PrintStream(OutputStream,boolean)`、`PrintStream(OutputStream,boolean,Charset)`、`PrintStream(String)`、`PrintStream(File)` 等
- 写: `write(int)`、`write(byte[],int,int)`、`writeBytes(byte[])`
- 打印: `print(...)`、`println(...)`、`printf(...)`、`format(...)`
- 追加: `append(char)`、`append(CharSequence)`、`append(CharSequence,int,int)`
- 控制: `checkError()`、`flush()`、`close()`、`charset()`、`autoFlush`
- 契约: 继承 FilterOutputStream、实现 `Appendable` + `Closeable`

## 2. Java 用户如何使用
- `new PrintStream(baos)` 把字节打印输出到内存流；`println` 输出行分隔符；`printf` 格式化；`charset()` 返回所用字符集。
- 组合链路：print/println/printf/append → flush → `baos.toByteArray()` 按 `charset()` 解码精确匹配。

## 3. 开源代码实际用法调研（真实片段）
- `PrintStream ps = new PrintStream(baos);` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:493`
- `PrintStream ps = new PrintStream(baos);` — `jdk25u-dev/test/jdk/tools/jimage/JImageCliTest.java:76`
- `PrintStream ps = new PrintStream(baos);` — `jdk25u-dev/test/jdk/tools/jmod/JmodNegativeTest.java:553`

## 4. 测试用例设计 + 自动化实现（指向 PrintStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: print(String/int/char/boolean)、println、printf("%s-%d")、format、append、write(byte[],off,len) → 按 `charset()` 解码后精确匹配（换行用 `System.lineSeparator()`）；checkError()==false。
- 边界: 空 print；非 ASCII 经 UTF-8 编解码正确。
- 健壮性: 显式 `StandardCharsets.UTF_8` 构造；行分隔符 `System.lineSeparator()`；内存流目标；无全局副作用。
- 白盒 `testWhiteBox`: 继承 FilterOutputStream、实现 Appendable+Closeable、printf/format/checkError/append/charset 存在。
