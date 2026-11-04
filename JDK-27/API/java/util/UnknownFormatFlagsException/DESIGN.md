# java.util.UnknownFormatFlagsException — 测试设计案

## 1. API 功能
使用了未知标志字符时抛出的 IllegalFormatException 子类（防御性异常）。构造器 String flags，getFlags()/getMessage()。

## 2. Java 用户如何使用
主要面向 Formatter 内部/编程式构造，getFlags() 返回非法标志字符串。链路：直接构造 -> getFlags/getMessage。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter`（本地 `~/git/jdk25u-dev`）对未知标志抛
  `new UnknownFormatFlagsException(flags)`，getMessage 返回 `"Flags = " + flags`——第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（UnknownFormatFlagsExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getFlags/getMessage 精确值 |
| 继承 | instanceof IllegalFormatException |

共 2 组场景、约 4 个 check（`testBlackBox()`）。
