# java.nio.charset.IllegalCharsetNameException 设计文档

## 1. API 功能

字符集名称格式非法（含空格等非法字符）时抛出的运行时异常。

- 构造器：`IllegalCharsetNameException(String charsetName)`（唯一构造器）。
- 继承链：`IllegalCharsetNameException` → `IllegalArgumentException` → `RuntimeException` → `Exception` → `Throwable`。
- 查询：`getCharsetName()` 返回非法名称；`getMessage()` 等于该名称。

## 2. Java 用户如何使用

- 捕获型用法：`Charset.forName("bad name!")` 时 `catch (IllegalCharsetNameException e)`，用 `e.getCharsetName()` 取回非法名称。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/build/.../gensrc/java.base/java/nio/charset/IllegalCharsetNameException.java`，模板生成）：构造器 `super(String.valueOf(charsetName))`，故 message 与 charsetName 一致。
- `Charset.forName` 在校验名称合法性失败时抛出该异常。

## 4. 测试用例设计 + 自动化实现

文件：`IllegalCharsetNameExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `IllegalArgumentException` / `String` 构造器。
- 黑盒：直接 `new` → `getCharsetName`/`getMessage` 一致 → 层级 → 无 cause → 堆栈非空；
  `Charset.forName("bad name!")` 触发该异常并保留名称。
