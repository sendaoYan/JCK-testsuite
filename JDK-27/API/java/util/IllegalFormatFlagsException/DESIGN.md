# java.util.IllegalFormatFlagsException — 测试设计案

## 1. API 功能
使用了该转换不支持的标志组合（如 `%+ d`）时抛出的 IllegalFormatException 子类。构造器 String flags，getFlags()/getMessage()。

## 2. Java 用户如何使用
格式化标志写错时捕获，用 getFlags() 定位。链路：Formatter.format -> 标志组合校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter`（本地 `~/git/jdk25u-dev`）检测非法标志组合抛
  `new IllegalFormatFlagsException(flags)`，getMessage 返回 `"Flags = '" + flags + "'"`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（IllegalFormatFlagsExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getFlags/getMessage 精确值 |
| 触发 | `%+ d` 抛 IllegalFormatFlagsException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
