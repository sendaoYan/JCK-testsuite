# java.util.MissingFormatArgumentException — 测试设计案

## 1. API 功能
格式说明符数量多于参数（如 `%d %s` 只传 1 个）时抛出的 IllegalFormatException 子类。构造器 String，getFormatSpecifier()/getMessage()。

## 2. Java 用户如何使用
格式化参数个数不足时捕获，用 getFormatSpecifier() 定位缺失参数。链路：Formatter.format -> 参数耗尽 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Formatter.transform`（本地 `~/git/jdk25u-dev`）参数不足抛
  `new MissingFormatArgumentException(fs)`，getMessage 返回 `"Format specifier '" + fs + "'"`——
  第 1/2/4 节对齐。

## 4. 测试用例设计 + 自动化实现（MissingFormatArgumentExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getFormatSpecifier/getMessage 精确值 |
| 触发 | `%d %s` + 1 参抛 MissingFormatArgumentException |
| 继承 | instanceof IllegalFormatException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
