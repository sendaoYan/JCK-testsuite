# java.nio.charset.UnsupportedCharsetException 设计文档

## 1. API 功能

请求的字符集名称合法但不受支持（未安装对应 provider）时抛出的运行时异常。

- 构造器：`UnsupportedCharsetException(String charsetName)`（唯一构造器）。
- 继承链：`UnsupportedCharsetException` → `IllegalArgumentException` → `RuntimeException` → `Exception` → `Throwable`。
- 查询：`getCharsetName()` 返回该名称；`getMessage()` 等于该名称。

## 2. Java 用户如何使用

- 捕获型用法：`Charset.forName("no-such-charset")` 时 `catch (UnsupportedCharsetException e)`，
  用 `e.getCharsetName()` 取回名称。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/build/.../gensrc/java.base/java/nio/charset/UnsupportedCharsetException.java`，模板生成）：构造器 `super(String.valueOf(charsetName))`。
- `Charset.forName` 在找不到 charset 时抛出该异常。

## 4. 测试用例设计 + 自动化实现

文件：`UnsupportedCharsetExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `IllegalArgumentException` / `String` 构造器。
- 黑盒：直接 `new` → `getCharsetName`/`getMessage` 一致 → 层级 → 无 cause → 堆栈非空；
  `Charset.forName("definitely-not-a-real-charset")` 触发该异常。
