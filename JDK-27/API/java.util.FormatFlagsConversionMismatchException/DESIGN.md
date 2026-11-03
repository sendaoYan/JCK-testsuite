# java.util.FormatFlagsConversionMismatchException — 测试设计案

## 1. API 功能
标志与转换类型不兼容（如 `%#s`）时抛出的 IllegalFormatException 子类。构造器 (String flags, char conversion)，getFlags()/getConversion()/getMessage()。

## 2. Java 用户如何使用
格式化字符串写错时捕获，用 getFlags()/getConversion() 定位冲突。链路：Formatter.format -> 标志/转换校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter.checkText`/`checkNumeric`（本地 `~/git/jdk25u-dev`）检测标志与转换不匹配时抛
  `new FormatFlagsConversionMismatchException(flags, conversion)`，getMessage 返回
  `"Conversion = " + conversion + ", Flags = " + flags`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（FormatFlagsConversionMismatchExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getFlags/getConversion/getMessage 精确值 |
| 触发 | `%#s` 抛 FormatFlagsConversionMismatchException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
