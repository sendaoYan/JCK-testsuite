# java.util.FormattableFlags — 测试设计案

## 1. API 功能
`Formattable.formatTo` 的 flags 位掩码常量类（工具类，私有构造）。

- 常量：`LEFT_JUSTIFY=1`、`UPPERCASE=2`、`ALTERNATE=4`。

## 2. Java 用户如何使用
在 Formattable.formatTo 里用位运算判断：`if ((flags & FormattableFlags.LEFT_JUSTIFY) != 0)`。
链路：常量取值 -> 位掩码组合 -> 与 formatTo 收到的 flags 对应。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/FormattableFlags.java`（本地 `~/git/jdk25u-dev`）：
  三个 public static final int 常量 + 私有构造——第 1 节对齐。
- OpenJDK `src/java.base/share/classes/java/util/Formatter.java`：解析格式串时把 `-`、`S`、
  `#` 映射为这些标志位传给 formatTo。

## 4. 测试用例设计 + 自动化实现（FormattableFlagsTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 精确值 | 1/2/4 |
| 位掩码 | 互不重叠、可 OR 组合为 7 |
| 实际传递 | %#S 传给 formatTo 的 flags == UPPERCASE|ALTERNATE |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
