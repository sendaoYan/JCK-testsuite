# java.util.FormatterClosedException — 测试设计案

## 1. API 功能
对已 close 的 Formatter 再次 format 时抛出的 IllegalStateException 子类。仅无参构造器。

## 2. Java 用户如何使用
复用已关闭的 Formatter 时捕获，避免误用。链路：Formatter.close -> format/toString -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter.ensureOpen`（本地 `~/git/jdk25u-dev`）在 closed 时
  `throw new FormatterClosedException()`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（FormatterClosedExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 继承 | extends IllegalStateException |
| 构造器 | 无参构造、消息 null |
| 触发 | close 后 format 抛 FormatterClosedException |

共 3 组场景、约 4 个 check（`testBlackBox()`）。
