# java.util.IllegalFormatPrecisionException — 测试设计案

## 1. API 功能
精度用于不支持的转换（如 `%.2d`）时抛出的 IllegalFormatException 子类。构造器 int，getPrecision()/getMessage()。

## 2. Java 用户如何使用
格式化精度写错时捕获，用 getPrecision() 定位。链路：Formatter.format -> 精度校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter`（本地 `~/git/jdk25u-dev`）检测不支持精度的转换抛
  `new IllegalFormatPrecisionException(precision)`，getMessage 返回 `String.valueOf(precision)`——
  第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（IllegalFormatPrecisionExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getPrecision/getMessage 精确值 |
| 触发 | `%.2d` 抛 IllegalFormatPrecisionException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
