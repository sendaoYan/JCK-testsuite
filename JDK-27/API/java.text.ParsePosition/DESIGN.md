# java.text.ParsePosition — 测试设计案

## 1. API 功能
解析位置游标：`Format.parseObject(String, ParsePosition)` 的标准“进度/错误”回传通道。

- 构造：`ParsePosition(int index)`（初始解析位置）。
- 查询/设置：`getIndex()/setIndex(int)`（当前游标）、`getErrorIndex()/setErrorIndex(int)`
  （失败位置，构造后默认 -1，表示“无错误”）。
- 对象协议：`equals/hashCode`（由 index+errorIndex 决定）、`toString`。

## 2. Java 用户如何使用
解析前 new 一个 ParsePosition 指定起点（常用于从长串中连续抠出多个值），交给
`format.parseObject(source, pos)`；成功则读 `pos.getIndex()`（下一轮起点），失败则读
`pos.getErrorIndex()`。链路：
`new ParsePosition(0) -> parseObject(s, pos) -> pos.getIndex() 前进 -> 复用同一 pos 再解析 ->
失败时 pos.getErrorIndex() >= 0 -> Format.parseObject(s) 抛 ParseException(getErrorOffset()==errorIndex)`。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/ParsePosition.java`（本地 `~/git/jdk25u-dev`）：
  toString 实现为 `getClass().getName() + "[index=" + index + ",errorIndex=" + errorIndex + ']'`，
  equals 比较 index 与 errorIndex——本测试第 2/3 节直接对齐。
- OpenJDK `src/java.base/share/classes/java/text/Format.java`：`parseObject(String)` 抛
  `ParseException("Format.parseObject: parse failed", pos.errorIndex)`——第 5 节验证该链路。
- OpenJDK `test/jdk/java/text/Format/common/FormatIteratorTest.java` 与
  `test/jdk/java/text/DecimalFormat/DecimalFormatTest.java` 大量使用
  `format.parse(s, new ParsePosition(0))` 断言 index/errorIndex 推进。
- Oracle javadoc
  (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/ParsePosition.html)
  给出的“在 pos 处解析、读 index 继续解析”用法即第 4 节场景。

## 4. 测试用例设计 + 自动化实现（ParsePositionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+游标 | ctor(5) getIndex==5；errorIndex 默认 -1；setIndex/setErrorIndex 可覆盖、可复位 |
| equals/hashCode | 等价状态 equals+hashCode 相同；index 与 errorIndex 各自破坏 equals；null/异型 false |
| toString | 精确 `"java.text.ParsePosition[index=7,errorIndex=2]"` 与默认 errorIndex 形态 |
| 真实解析链路 | DecimalFormat(US) 解析 "1,234.5xyz"：值 1234.5、index 7、errorIndex -1；
  从 index=2 续解 "ab34.5" 得 34.5、index 6；失败 null 且 index 不动、errorIndex=0 |
| 与 ParseException 衔接 | parseObject("xyz") 抛 ParseException 且 getErrorOffset()==errorIndex |

共 5 组场景、约 20 个 check（`testBlackBox()`）。
