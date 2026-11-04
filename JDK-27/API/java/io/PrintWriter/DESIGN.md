# java.io.PrintWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造: `PrintWriter(Writer)`、`PrintWriter(Writer,boolean)`、`PrintWriter(OutputStream)`、`PrintWriter(OutputStream,boolean)`、`PrintWriter(String)`、`PrintWriter(File)`、`PrintWriter(Writer,boolean)` + Charset 变体
- 写: `write(int)`、`write(char[],int,int)`、`write(String,int,int)`
- 打印: `print(...)`、`println(...)`、`printf(String,Object...)`、`format(String,Object...)`
- 追加: `append(char)`、`append(CharSequence)`、`append(CharSequence,int,int)`
- 控制: `checkError()`、`setError()`、`flush()`、`close()`、`autoFlush`

## 2. Java 用户如何使用
- `new PrintWriter(writer)` 把格式化文本输出到 Writer；`printf/format` 用 `String.format` 规则；`println` 输出行分隔符。
- 组合链路：`PrintWriter(StringWriter)` → print/println/printf → `checkError()`==false → StringWriter.toString() 精确匹配（行分隔符用 `System.lineSeparator()`）。

## 3. 开源代码实际用法调研（真实片段）
- `new PrintWriter(writer)` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:563`
- `PrintWriter pw = new PrintWriter(writer);` — `jdk25u-dev/test/jdk/tools/jlink/bindservices/BindServices.java:207`
- `PrintWriter pw = new PrintWriter(writer);` — `jdk25u-dev/test/jdk/tools/jlink/bindservices/SuggestProviders.java:312`

## 4. 测试用例设计 + 自动化实现（指向 PrintWriterTest.java 黑盒 `testBlackBox`）
- 正常/组合: print(String/int/char/boolean)、println、printf("%s-%d")、format、append → StringWriter.toString() 精确（换行按 `System.lineSeparator()`）；checkError()==false；write(char[],off,len)。
- 边界: 空 print；printf 格式化数值/字符串。
- 健壮性: 行分隔符统一用 `System.lineSeparator()` 跨平台；内存流目标；无全局副作用。
- 白盒 `testWhiteBox`: 继承 Writer、printf/format/checkError/append/setError 存在。
