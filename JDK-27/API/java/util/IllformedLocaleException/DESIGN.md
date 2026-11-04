# java.util.IllformedLocaleException — 测试设计案

## 1. API 功能
`Locale.Builder` 收到格式错误的语言/地区/脚本时抛出的运行时异常。构造器 ()、String、(String, int errorIndex)，getErrorIndex()。

## 2. Java 用户如何使用
构造 Locale 时语言标签非法捕获；getErrorIndex() 定位非法位置。链路：Locale.Builder.setLanguage -> 校验 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Locale.Builder`（本地 `~/git/jdk25u-dev`）校验失败抛
  `new IllformedLocaleException(message, index)`——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/Locale/`.

## 4. 测试用例设计 + 自动化实现（IllformedLocaleExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getErrorIndex（含默认 -1）精确值 |
| 触发 | setLanguage("abcdefghi") 抛 IllformedLocaleException |
| 继承 | extends RuntimeException |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
