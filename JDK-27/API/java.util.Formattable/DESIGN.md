# java.util.Formattable — 测试设计案

## 1. API 功能
让自定义类型参与 `Formatter` 的 `%s`/`%S` 等转换：实现 `formatTo(Formatter, flags, width, precision)`。

- 抽象：`formatTo(Formatter, int flags, int width, int precision)`。
- 语义：Formatter 遇到 Formattable 对象时调用 formatTo，把 flags（FormattableFlags 位掩码）、
  width、precision（未指定为 -1）传入。

## 2. Java 用户如何使用
`class X implements Formattable { void formatTo(Formatter f, int flags, int w, int p) {
f.format(...); } }`，然后 `String.format("%-10.4s", new X())` 触发。
链路：实现 formatTo -> String.format("%s/%S/%#s/%w.p s") -> 观察 flags/width/precision 传入。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/Formattable.java`（本地 `~/git/jdk25u-dev`）：
  javadoc 说明 `%s` 时 flags=0、width/precision=-1；`%S` 置 UPPERCASE；`%#` 置 ALTERNATE——
  第 2/3/4 节对齐。
- OpenJDK `src/java.base/share/classes/java/util/Formatter.java`：`transform` 对 Formattable 调用
  formatTo 并传入解析后的 flags/width/precision。
- OpenJDK `test/jdk/java/util/Formattable/` 有官方测试。

## 4. 测试用例设计 + 自动化实现（FormattableTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 直接调用 | formatTo 写入 Formatter |
| %s | flags=0、width/precision=-1 |
| %-10.4s | LEFT_JUSTIFY + width=10 + precision=4 |
| %S / %#S | UPPERCASE / UPPERCASE|ALTERNATE |
| 常量 | LEFT_JUSTIFY=1、UPPERCASE=2、ALTERNATE=4，可按位组合 |

共 5 组场景、约 10 个 check（`testBlackBox()`）。
