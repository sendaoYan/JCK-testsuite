# java.nio.charset.UnmappableCharacterException 设计文档

## 1. API 功能

编码时遇到目标字符集无法映射的字符（unmappable character）时抛出的受检异常。

- 构造器：`UnmappableCharacterException(int inputLength)`（唯一构造器）。
- 继承链：`UnmappableCharacterException` → `CharacterCodingException` → `IOException` → `Exception` → `Throwable`。
- 查询：`getInputLength()` 返回不可映射序列长度；`getMessage()` 返回 `"Input length = " + inputLength`。

## 2. Java 用户如何使用

- 捕获型用法：`CharsetEncoder` 的 report 路径遇到不可映射字符时抛 `UnmappableCharacterException`，
  用户用 `getInputLength()` 获取长度。
- 组合链路：`CoderResult.unmappableForLength(n)` → `throwException()` 抛 `UnmappableCharacterException(n)`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/UnmappableCharacterException.java`）：`getMessage()` 固定返回 `"Input length = " + inputLength`。
- `CoderResult.throwException()` 的 `CR_UNMAPPABLE` 分支抛出该异常。

## 4. 测试用例设计 + 自动化实现

文件：`UnmappableCharacterExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `CharacterCodingException` / `int` 构造器。
- 黑盒：构造→`getInputLength`→`getMessage`→层级→无 cause→堆栈非空。
