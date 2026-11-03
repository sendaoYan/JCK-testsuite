# java.util.DuplicateFormatFlagsException — 测试设计案

## 1. API 功能
格式说明符中重复使用同一标志（如 `%,,d`）时抛出的 IllegalFormatException 子类。构造器 String flags，getFlags()/getMessage()。

## 2. Java 用户如何使用
格式化字符串写错时捕获，用 getFlags() 定位重复标志。链路：Formatter.format -> 标志去重校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter.parse`（本地 `~/git/jdk25u-dev`）检测重复标志抛
  `new DuplicateFormatFlagsException(flags)`，getMessage 返回 `"Flags = '" + flags + "'"`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（DuplicateFormatFlagsExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getFlags/getMessage 回传精确值 |
| 触发 | `%,,d` 抛 DuplicateFormatFlagsException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
