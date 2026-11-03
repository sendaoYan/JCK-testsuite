# java.util.IllegalFormatConversionException — 测试设计案

## 1. API 功能
参数类型与转换不匹配（如 `%d` 传 String）时抛出的 IllegalFormatException 子类。构造器 (char, Class<?>)，getConversion()/getArgumentClass()/getMessage()。

## 2. Java 用户如何使用
格式化参数类型写错时捕获，用 getConversion()/getArgumentClass() 定位。链路：Formatter.format -> 类型校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter.transform`（本地 `~/git/jdk25u-dev`）类型不符抛
  `new IllegalFormatConversionException(conversion, arg.getClass())`，getMessage 返回
  `"<conv> != <class>"`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（IllegalFormatConversionExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getConversion/getArgumentClass/getMessage 精确值 |
| 触发 | `%d` + String 抛 IllegalFormatConversionException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
