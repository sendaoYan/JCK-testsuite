# java.nio.charset.CoderMalfunctionError 测试方案

## 1. API 功能

表示 `CharsetDecoder.decodeLoop` / `CharsetEncoder.encodeLoop` 回调内部发生功能异常（非预期异常）时抛出的 `Error`。

- 构造器：`CoderMalfunctionError(Exception cause)`（唯一构造器），直接 `super(cause)`。
- 继承链：`CoderMalfunctionError` → `Error` → `Throwable`。
- 查询/链方法：`getCause()` 返回构造时传入的 `Exception`；`getMessage()` 采用 `Throwable(Throwable)` 默认语义——cause 非空时为 `cause.toString()`，cause 为 null 时 message 为 null（注意：不是字符串 "null"）。
- Serializable（经 Throwable），`serialVersionUID == -1151412348057794301L`（实测 JDK-8 与 JDK-27 一致）。

## 2. Java 用户如何使用

- 捕获型用法：自定义 CharsetDecoder/Encoder 子类的 `*Loop` 方法抛出异常时，`decode`/`encode` 引擎将其包装为 `CoderMalfunctionError` 抛出，用户通过 `getCause()` 取回原始异常。
- 触发场景：`decodeLoop` 抛出 `BufferOverflowException`/`BufferUnderflowException`（如对 out 缓冲的误操作）时被引擎包装。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk8u-dev/jdk/src/share/classes/java/nio/charset/Charset-X-Coder.java.template`，
  `$code`Loop 调用点的 try/catch）：**JDK-8 只 catch `BufferUnderflowException` 与 `BufferOverflowException`**
  并包装为 `CoderMalfunctionError`；其它任意 RuntimeException（如 ArithmeticException）不被包装、原样穿透。
- JDK-27 的同名模板改为 `catch (RuntimeException x)`，任意未检查异常都会被包装——两侧实测确认该差异
  （见 JDK-27 侧 DESIGN 与测试内注释）。
- 回归参考：`~/git/jdk/test/jdk/java/nio/charset/*/` 下自定义 coder 的错误注入思路。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`CoderMalfunctionErrorTest.java`（testCoderMalfunctionError 保留 + testWhiteBox + testCtorMatrix +
testRealTriggerDecodeLoop + testRealTriggerEncodeLoop + testWrapScopeJdk8 + testSerialization）。

- 白盒：public / 非 final / 父类 `Error` / `Exception` 构造器 / Serializable。
- 构造矩阵：cause 同一性、message == cause.toString()、toString 精确格式、null cause → getCause null 且
  message null（Throwable(Throwable) 语义）、构造后再次 initCause → IllegalStateException。
- 真实触发（契约级）：自定义 `BoomDecoder`/`BoomEncoder`（super(cs,1,1)，覆盖 `decodeLoop`/`encodeLoop`
  抛 `BufferOverflowException`/`BufferUnderflowException`）→ `decode`/`encode`（3 参与便捷版）抛
  `CoderMalfunctionError` 且 cause 为同一异常实例。
- JDK-8 版本特有：`decodeLoop` 抛 `ArithmeticException` → **原样穿透不被包装**（模板 catch 范围实测确认）。
- 序列化：suid 精确断言 + round-trip 后 cause/message 保持。

健壮性：无文件/网络/线程/墙钟/随机/Locale 依赖；只用标准字符集构造 coder；异常注入完全确定性。
