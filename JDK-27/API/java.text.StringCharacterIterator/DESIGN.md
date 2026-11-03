# java.text.StringCharacterIterator — 测试设计案

## 1. API 功能
`CharacterIterator` 在 `String` 上的实现：在可选的 `[begin, end)` 子区间内做双向游标遍历。

- 构造：`StringCharacterIterator(String)`（全文）；`(String, int pos)`（全文+初始位置）；
  `(String, int begin, int end, int pos)`（子区间+初始位置）。
- 遍历（实现接口）：first/last/current/next/previous/setIndex/getBeginIndex/getEndIndex/getIndex。
- 其他：`setText(String)`（换文本并复位游标）、`equals/hashCode`（text+pos+begin+end 四元组）、
  `clone()`（深拷贝位置，独立可变）。

## 2. Java 用户如何使用
双向扫描字符串：`StringCharacterIterator it = new StringCharacterIterator(s); for (char c =
it.first(); c != DONE; c = it.next()) ...`；限定子串用 4 参构造；重用它处理多段文本用 setText。
链路：构造 -> first/last/next/previous/setIndex 往返 -> getIndex/getBegin/End 定位 ->
equals/clone 比较与拷贝 -> setText 换文本重跑。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/StringCharacterIterator.java`（本地
  `~/git/jdk25u-dev`）：`setText` 用 `Objects.requireNonNull` 抛 NPE、并复位 begin=0/end=len/pos=0；
  equals 比较 text+pos+begin+end 且先比 hashCode——第 4/6/8 节断言对齐；4 参构造校验
  `begin<0 || begin>end || end>length` 抛 `IllegalArgumentException("Invalid substring range")`、
  `pos<begin || pos>end` 抛 `IllegalArgumentException("Invalid position")`。
- OpenJDK `test/jdk/java/text/Collator/Regression.java`、`BreakIteratorTest.java` 等内部大量
  用 StringCharacterIterator 作为 BreakIterator/Collator 的文本游标。
- Apache Commons/ICU（同源 CharacterIterator）：文本分词/边界检测以该接口描述文本——典型下游。

## 4. 测试用例设计 + 自动化实现（StringCharacterIteratorTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| String 构造 | first/last、begin/end、setIndex/current/next/previous/getIndex |
| (String,int) 构造 | 初始 index、全文范围、当前位置 current |
| 4 参构造 | begin/end/index 初始值；first 钳到 begin、last 钳到 end-1 |
| setText | 换文本、重算 bounds、复位 index |
| clone | 独立副本、equals/hashCode 一致、保留 index/bounds、改副本不影响原 |
| equals/hashCode | 等价实例相等；index/text/bounds 参与相等；null/异型 false |
| DONE/空文本 | next 越界 DONE、current DONE；空文本 first DONE |
| 错误路径 | pos 越界/begin>end/end 越界抛 IllegalArgumentException；setIndex 越界；
  setText(null) 抛 NPE |

共 8 组场景、约 28 个 check（`testBlackBox()`）。
