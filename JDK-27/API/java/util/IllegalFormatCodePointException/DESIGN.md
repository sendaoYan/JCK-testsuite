# java.util.IllegalFormatCodePointException — 测试设计案

## 1. API 功能
`%c` 转换的码点非法（不在 0..0x10FFFF 或为代理项）时抛出的 IllegalFormatException 子类。构造器 int，getCodePoint()/getMessage()。

## 2. Java 用户如何使用
格式化非法字符码点时捕获，用 getCodePoint() 定位。链路：Formatter.format -> `%c` 码点校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter.printCharacter`（本地 `~/git/jdk25u-dev`）对越界码点抛
  `new IllegalFormatCodePointException(codePoint)`，getMessage 返回 `"Code point = " + codePoint`——
  第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（IllegalFormatCodePointExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getCodePoint/getMessage 精确值 |
| 触发 | `%c` + 0x110000 抛 IllegalFormatCodePointException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 4 个 check（`testBlackBox()`）。
