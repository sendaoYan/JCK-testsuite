# java.util.UnknownFormatConversionException — 测试设计案

## 1. API 功能
使用了未知转换字符（如 `%y`）时抛出的 IllegalFormatException 子类。构造器 String，getConversion()/getMessage()。

## 2. Java 用户如何使用
格式转换字符写错时捕获，用 getConversion() 定位。链路：Formatter.format -> 转换字符校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter`（本地 `~/git/jdk25u-dev`）检测未知转换抛
  `new UnknownFormatConversionException(String.valueOf(conversion))`，getMessage 返回
  `"Conversion = '" + c + "'"`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（UnknownFormatConversionExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getConversion/getMessage 精确值 |
| 触发 | `%y` 抛 UnknownFormatConversionException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
