# java.util.IllegalFormatWidthException — 测试设计案

## 1. API 功能
宽度为负数时抛出的 IllegalFormatException 子类（防御性异常，格式字符串解析不会产生负宽度）。构造器 int，getWidth()/getMessage()。

## 2. Java 用户如何使用
主要用于 Formatter 内部/编程式构造，getWidth() 返回负宽度。链路：直接构造 -> getWidth/getMessage。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter`（本地 `~/git/jdk25u-dev`）对负宽度抛
  `new IllegalFormatWidthException(width)`，getMessage 返回 `String.valueOf(width)`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（IllegalFormatWidthExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getWidth/getMessage 精确值 |
| 继承 | instanceof IllegalFormatException |
| 负值语义 | 保留负宽度 |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
