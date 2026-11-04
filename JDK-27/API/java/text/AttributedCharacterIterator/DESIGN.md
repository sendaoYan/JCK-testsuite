# java.text.AttributedCharacterIterator — 测试设计案

## 1. API 功能
带属性文本的迭代器：继承 `CharacterIterator` 的全部游标操作，额外提供**属性 run**查询。
与 `CharacterIterator` 不同，它描述的不只是字符，还有每个位置生效的属性集。

- 游标（继承）：first/last/current/next/previous/setIndex/getBeginIndex/getEndIndex/getIndex/clone。
- 综合 run 边界：`getRunStart()/getRunLimit()`（当前位置所属的、属性集完全一致的最长区间）。
- 单属性 run：`getRunStart(Attribute)/getRunLimit(Attribute)`（只考虑某属性值连续相等的区间，
  该属性在当前位置不存在时返回紧邻前一个 run 的边界）。
- 属性集 run：`getRunStart(Set)/getRunLimit(Set)`（只考虑集合中出现的属性）。
- 属性查询：`getAttributes()`（当前位置生效属性 Map）、`getAttribute(Attribute)`（单值）、
  `getAllAttributeKeys()`（全文本出现过的属性键集合）。

## 2. Java 用户如何使用
先由 `AttributedString.getIterator()`（或具体 Format 的 `formatToCharacterIterator`）得到
iterator，然后 `for(char c = it.first(); c != DONE; c = it.next()) { it.getAttribute(...);
it.getRunStart()/getRunLimit(); }` 遍历每一位置、按 run 切段、抽取带属性的文本片段。
链路：AttributedString 多段 addAttribute -> getIterator -> 游标遍历 -> 每位置 getAttributes/
getAttribute -> run 边界聚合（整体/单属性/属性集三档）。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/AttributedString.java` 内部
  `AttributedStringIterator`（本地 `~/git/jdk25u-dev`）：`getRunStart(Attribute)` 实现是沿
  `runStarts` 回溯比较属性值、越过 beginIndex 则钳位到 beginIndex——第 3/4 节断言与其一致。
- OpenJDK `src/java.base/share/classes/java/text/AttributedString.java#getAttributes`：
  当前 run 无属性时返回空 Hashtable（非 null）——第 7 节 plain 文本断言 getAttributes().isEmpty()。
- OpenJDK `test/jdk/java/text/AttributedString/getRunStartLimitTest.java`：官方测试分别
  addAttribute 后断言 getRunStart/Limit 与每段属性。
- OpenJDK `test/jdk/java/text/Format/common/FormatIteratorTest.java`：遍历 formatToCharacterIterator
  返回的 iterator 断言 run/属性——是带属性文本的权威消费示例。

## 4. 测试用例设计 + 自动化实现（AttributedCharacterIteratorTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 游标重建文本 | first/next 循环拼出 "0123456789"，begin=0/end=10 |
| 综合 run 边界 | LANGUAGE [0,4)[4,10)、READING [2,8) 叠加出 4 段 run：[0,2)[2,4)[4,8)[8,10)，
  遍历 10 个位置逐一断言 getRunStart/getRunLimit |
| 单属性 run | index0 处 LANG run [0,4)、READ run [2,8)（缺省值回溯到下一 run 起点）；
  index6 处 LANG [4,10)、READ [2,8) |
| 属性集 run | {LANG,READ} 在 0 处 run [0,2)（READ 在 index2 出现即变）；在 6 处 run [4,8)；仅 {READ} 在 6 处 run [2,8) |
| 位置属性 | index1 只有 en；index3 en+r1；index9 只有 fr；last 处 attributes 含 fr |
| getAllAttributeKeys | 两个键（LANG/READ），稳定 |
| 无属性文本 | 空 keys、空 attributes、getAttribute null、单一 run [0,5) |
| clone | 经 CharacterIterator 契约可 clone |

共 8 组场景、约 25 个 check（`testBlackBox()`）。

## 契约深化（本批新增，两侧同构断言）

- Attribute 常量 toString 精确 + Format.Field 族子类关系
- 组合 run 边界表（READING [0,5) + LANG 全文 → runs [0,5)/[5,11)）；缺席属性 run=最大稳定跨度
- 过滤迭代器（指定属性子集 + 范围 [6,11)：隐藏未列出属性、getAllAttributeKeys 只列已选、setIndex 边界 IAE）
- Annotation 属性值经迭代器往返
