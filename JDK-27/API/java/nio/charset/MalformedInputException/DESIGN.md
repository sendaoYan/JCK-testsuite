# java.nio.charset.MalformedInputException 设计文档

## 1. API 功能

解码输入中包含非法字节序列（malformed input）时抛出的受检异常。

- 构造器：`MalformedInputException(int inputLength)`（唯一构造器）。
- 继承链：`MalformedInputException` → `CharacterCodingException` → `IOException` → `Exception` → `Throwable`。
- 查询：`getInputLength()` 返回非法序列长度；`getMessage()` 返回 `"Input length = " + inputLength`。

## 2. Java 用户如何使用

- 捕获型用法：`CharsetDecoder` 的 report 路径解码非法字节时抛 `MalformedInputException`，用户用 `getInputLength()` 获取非法输入长度。
- 组合链路：`CoderResult.malformedForLength(n)` → `throwException()` 抛 `MalformedInputException(n)`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/MalformedInputException.java`）：`getMessage()` 固定返回 `"Input length = " + inputLength`。
- `CoderResult.throwException()` 的 `CR_MALFORMED` 分支抛出该异常。

## 4. 测试用例设计 + 自动化实现

文件：`MalformedInputExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `CharacterCodingException` / `int` 构造器。
- 黑盒：构造→`getInputLength`→`getMessage`→层级→无 cause→堆栈非空。
