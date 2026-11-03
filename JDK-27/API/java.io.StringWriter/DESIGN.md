# java.io.StringWriter 测试方案 (DESIGN)

## 1. API 功能
- 构造: `StringWriter()`、`StringWriter(int)`
- 写: `write(int)`、`write(char[],int,int)`、`write(String)`、`write(String,int,int)`
- 追加: `append(char)`、`append(CharSequence)`、`append(CharSequence,int,int)`
- 读回/状态: `toString()`、`getBuffer()`、`flush()`、`close()`

## 2. Java 用户如何使用
- `new StringWriter()` 累积字符，`toString()` 取回；`getBuffer()` 直接操作底层 StringBuffer。
- 组合链路：write/append 多段 → `toString()` 等值 → `PrintWriter` 包装做格式化输出。

## 3. 开源代码实际用法调研（真实片段）
- `StringWriter writer = new StringWriter();` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:562`
- `StringWriter writer = new StringWriter();` — `jdk25u-dev/test/jdk/tools/jlink/bindservices/BindServices.java:206`
- `StringWriter writer = new StringWriter();` — `jdk25u-dev/test/jdk/tools/jlink/bindservices/SuggestProviders.java:311`

## 4. 测试用例设计 + 自动化实现（指向 StringWriterTest.java 黑盒 `testBlackBox`）
- 正常/组合: write(int)/write(char[],off,len)/write(String,int,int)/append 累积 → toString 精确；getBuffer 内容一致；flush 无副作用；close 后 toString 仍可用。
- 边界: 空写；append(CharSequence,off,len) 子区间。
- 健壮性: 内存累积，无全局副作用；固定输入。
- 白盒 `testWhiteBox`: 继承 Writer、toString/getBuffer/append/flush 存在。
