# java.util.zip.ZipError 测试方案

白盒结构断言在 `ZipErrorTest.testWhiteBox()`，黑盒行为断言在 `ZipErrorTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`ZipError(String s)`（唯一构造器）。
- 继承：`InternalError` → `VirtualMachineError` → `Error` → `Throwable`。已废弃、不再被 ZIP 实现使用（源码标注 obsolete）。

## 2. Java 用户如何使用

- 一般不需要用户直接抛/捕获；历史上用于表示 ZIP 处理的严重内部错误。
- 典型调用：`throw new ZipError("corrupt zip internals");` 或捕获后作为致命错误处理。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/ZipError.java:31`：`@deprecated ZipError is no longer used and is obsolete.` —— 官方废弃说明。
- `src/java.base/share/classes/java/util/zip/ZipError.java:37`：`public class ZipError extends InternalError` —— 继承关系。
- `src/java.base/share/classes/java/util/zip/ZipError.java:45`：`public ZipError(String s)` —— 唯一构造器。

## 4. 测试用例设计 + 自动化实现

- **正常**：`ZipError("boom")` 的 `getMessage()` 为 "boom"、`getCause()` null。
- **继承**：`instanceof Error/InternalError/VirtualMachineError`。
- **自动化**：`ZipErrorTest.testBlackBox()`；`testWhiteBox()` 校验 `getSuperclass()==InternalError.class` 与 `String` 构造器存在。
- **健壮性**：无副作用、headless、无 `@key`。
