# java.lang.reflect.MalformedParameterizedTypeException — 测试设计（JDK-27）

## 1. API 功能
`MalformedParameterizedTypeException extends RuntimeException`（非受检）：
- 构造器：无参、`(String message)`（JDK-21 新增）。
- 语义：反射机制遇到签名畸形的参数化类型时抛出（用户侧多直接构造用于错误汇报）。

## 2. Java 用户如何使用
泛型签名校验失败时抛出/传递；按 RuntimeException 捕获并读 `getMessage()`。与 `GenericSignatureFormatError`（Error 层）相对，本类是 RuntimeException 层的畸形类型信号。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/MalformedParameterizedTypeException.java`（含 JDK-21 新构造器的注释）。
- `sun.reflect.generics` 解析器异常抛出路径（`~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/generics/factory/`）。
- 字节码库（ASM `SignatureReader` 邻接）对畸形泛型签名的包装上报同构（场景 4）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MalformedParameterizedTypeExceptionTest.testBlackBox`）：
1. 裸构造：消息/cause null、toString 精确全名。
2. 消息构造（JDK-21+）：往返、toString 含消息、`getLocalizedMessage` 默认同值。
3. `initCause` 接线 + `addSuppressed` 挂其他类型异常。
4. 非受检 throw/catch。
5. 层次：直接子类 RuntimeException；构造即捕获栈、`fillInStackTrace` 返回自身、程序化 `setStackTrace` 生效（固定行号断言）。
白盒保留原有结构断言。产出：`MalformedParameterizedTypeExceptionTest.java`（16 断言通过）。

## 深化批次（2026-11-05）

新增方法（断言 17 → 28）：

| 测试点 | 方法 |
|---|---|
| Serializable 普查、构造器数 2（JDK-8 为 1 的版本差异）、bare toString 精确 | testSerializableCensus |
| 序列化 round-trip：消息/cause/suppressed 保真 | testSerializationRoundTrip |
| printStackTrace 渲染类型/消息/cause | testStackPrinting |
