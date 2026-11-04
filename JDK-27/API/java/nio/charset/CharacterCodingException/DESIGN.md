# java.nio.charset.CharacterCodingException 设计文档

## 1. API 功能

字符集编解码过程中发生错误时抛出的受检异常，是 `MalformedInputException` 与 `UnmappableCharacterException` 的父类。

- 构造器：`CharacterCodingException()`（唯一构造器，无 message）。
- 继承链：`CharacterCodingException` → `IOException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`CoderResult.throwException()` 或 `CharsetDecoder/Encoder` 的 report 路径抛出，
  用户 `catch (CharacterCodingException e)` 统一处理编解码错误。
- 组合链路：`CharsetEncoder.onUnmappableCharacter(REPORT)` → `encode(...)` 得 `CoderResult` → `throwException()` 抛该异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/CoderResult.java`）：`throwException()` 的 `CR_MALFORMED`/`CR_UNMAPPABLE` 分支抛出 `MalformedInputException`/`UnmappableCharacterException`（两者均为 `CharacterCodingException` 子类）。

## 4. 测试用例设计 + 自动化实现

文件：`CharacterCodingExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `IOException` / 无参构造器。
- 黑盒：直接 `new` → message null → cause null → 堆栈非空；
  用 US-ASCII 编码器对 `€`（不可映射）编码得到 unmappable `CoderResult` → `throwException()` 抛出 `CharacterCodingException`（实际为 `UnmappableCharacterException`）。
