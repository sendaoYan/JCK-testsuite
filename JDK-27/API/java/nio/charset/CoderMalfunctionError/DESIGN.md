# java.nio.charset.CoderMalfunctionError 设计文档

## 1. API 功能

表示 `CharsetEncoder/Decoder` 回调中抛出的非预期异常（coder 功能异常）的 Error。

- 构造器：`CoderMalfunctionError(Exception cause)`（唯一构造器）。
- 继承链：`CoderMalfunctionError` → `Error` → `Throwable`。
- 查询/链方法：`getCause()` 返回构造时传入的 `Exception`；`getMessage()` 等于 `cause.toString()`。

## 2. Java 用户如何使用

- 捕获型用法：自定义 coder 回调抛出 `Exception` 时，编解码器用 `CoderMalfunctionError` 包装后抛出；
  用户通过 `getCause()` 取得原始 `Exception`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/charset/CoderMalfunctionError.java`）：构造器直接 `super(cause)`，因此 message 采用 `Throwable(Throwable)` 的默认语义（`cause.toString()`）。

## 4. 测试用例设计 + 自动化实现

文件：`CoderMalfunctionErrorTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `Error` / `Exception` 构造器。
- 黑盒：构造→`Error` 层级→`getCause()` 返回原异常→`getMessage()` 等于 `cause.toString()`→堆栈非空。

## 深化追加（批次 A4，契约级）

- 构造矩阵：cause 同一性、message == cause.toString()、toString 精确格式、null cause → getCause null 且
  message null（Throwable(Throwable) 语义，非字面量 "null"）、二次 initCause → IllegalStateException。
- 真实触发（契约级）：自定义 BoomDecoder/BoomEncoder（覆盖 decodeLoop/encodeLoop 抛
  BufferOverflowException/BufferUnderflowException）→ decode/encode（3 参与便捷版）抛 CoderMalfunctionError
  且 cause 为同一异常实例。
- **JDK-27 版本特有**：coder 模板为 `catch (RuntimeException x)`（jdk27 Charset-X-Coder.java.template），
  decodeLoop 抛任意 RuntimeException（如 ArithmeticException）都会被包装为 CoderMalfunctionError；
  JDK-8 只包装 BufferUnderflow/BufferOverflow（各自断言，见 JDK-8 侧 testWrapScopeJdk8）。
- 序列化：serialVersionUID == -1151412348057794301L（双侧实测一致）；round-trip 后 cause/message 保持。
