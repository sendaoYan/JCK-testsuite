# java.util.MissingFormatWidthException — 测试设计案

## 1. API 功能
要求宽度的转换缺少宽度（如 `%-d`）时抛出的 IllegalFormatException 子类。构造器 String，getFormatSpecifier()/getMessage()。

## 2. Java 用户如何使用
格式化宽度缺失时捕获，用 getFormatSpecifier() 定位。链路：Formatter.format -> 宽度校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter`（本地 `~/git/jdk25u-dev`）检测缺失宽度抛
  `new MissingFormatWidthException(fs)`，getMessage 返回 `fs` 本身——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（MissingFormatWidthExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getFormatSpecifier/getMessage 精确值 |
| 触发 | `%-d` 抛 MissingFormatWidthException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
