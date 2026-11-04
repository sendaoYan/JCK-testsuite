# java.util.logging.ErrorManager 测试方案

白盒结构断言在 `ErrorManagerTest.testWhiteBox()`，黑盒行为断言在 `ErrorManagerTest.testBlackBox()`。

## 1. API 功能

- **常量（public static final int）**：`GENERIC_FAILURE=0`、`WRITE_FAILURE=1`、`FLUSH_FAILURE=2`、`CLOSE_FAILURE=3`、`OPEN_FAILURE=4`、`FORMAT_FAILURE=5`。
- **构造器**：无参 `ErrorManager()`。
- **核心方法**：`error(String msg, Exception ex, int code)` —— 仅报告首次错误（内部 `reported` 标志），随后调用被忽略；首次报告会向 `System.err` 输出诊断。

## 2. Java 用户如何使用

- 作为 Handler 的错误处理回调：`handler.setErrorManager(new ErrorManager());`
- 在 Handler 内部 `reportError` 时被调用；用户通常不直接调用 `error`，但可自定义子类改写错误处理策略。

## 3. 开源代码实际用法调研

> WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev`，逐条标注来源。

- `src/java.logging/share/classes/java/util/logging/Handler.java:90`：`private volatile ErrorManager errorManager = new ErrorManager();` —— Handler 默认安装。
- `src/java.logging/share/classes/java/util/logging/Handler.java:272-281`：`reportError` 调用 `errorManager.error(...)` —— 组合点。
- `src/java.logging/share/classes/java/util/logging/ErrorManager.java:90-100`：`error` 实现（reported 标志 + System.err 输出）。

## 4. 测试用例设计 + 自动化实现

- **正常**：6 个常量精确值断言。
- **组合/边界**：重定向 `System.err`（finally 恢复）捕获 `error()` 输出，断言包含 `ErrorManager` 与失败码；同一实例第二次 `error` 被抑制（输出不变）；新实例会再次报告。
- **健壮性**：临时重定向 System.err 并在 finally 恢复，无其它全局副作用。
