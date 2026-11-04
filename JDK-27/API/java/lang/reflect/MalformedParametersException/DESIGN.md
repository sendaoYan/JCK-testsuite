# java.lang.reflect.MalformedParametersException — 测试设计（JDK-27）

## 1. API 功能
`MalformedParametersException extends RuntimeException`（非受检）：
- 构造器：无参、`(String message)`。
- 由反射机制在类文件的 `MethodParameters` 属性畸形时抛出（用户侧主要直接构造用于错误汇报）。
- 全套 `Throwable` 行为（cause/suppressed/stack trace/toString）。

## 2. Java 用户如何使用
字节码工具/类加载校验器检测到非法参数属性时抛出并携带描述消息；catch 侧按 RuntimeException 处理并取 `getMessage()`。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/MalformedParametersException.java`（javadoc 指明触发场景）。
- JDK `Executable.parseParameterAnnotations`/`Parameter` 源路径为实际抛出点（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/Executable.java`）。
- ASM/ByteBuddy 类工具链以同语义异常包装字节码校验失败（同构场景 4 的 throw/catch 用法）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MalformedParametersExceptionTest.testBlackBox`）：
1. 裸构造：消息/cause/本地化消息 null、栈可访问。
2. 消息构造：往返、toString 前缀与消息内容。
3. Throwable 机制：`initCause` 接线、`addSuppressed` 挂同类异常。
4. 非受检语义：throw 后以 RuntimeException 捕获且消息保持。
5. 直接继承 RuntimeException（层次）。
6. `fillInStackTrace` 返回自身且栈已捕获。
白盒保留原有两构造器断言。产出：`MalformedParametersExceptionTest.java`（19 断言通过）。

## 深化批次（2026-11-05）

新增方法（断言 20 → 32）：

| 测试点 | 方法 |
|---|---|
| Serializable 普查、bare/带消息 toString 两种精确形态 | testSerializableCensus |
| 序列化 round-trip：消息/cause/suppressed 保真 | testSerializationRoundTrip |
| printStackTrace 渲染 | testStackPrinting |
