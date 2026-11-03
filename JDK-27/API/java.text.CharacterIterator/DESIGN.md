# java.text.CharacterIterator — 测试设计案

## 1. API 功能
字符串/文本的双向游标接口：在 `[beginIndex, endIndex)` 上移动并读当前字符。

- 边界：`getBeginIndex()/getEndIndex()/getIndex()`。
- 移动：`first()/last()/next()/previous()/setIndex(int)`（越界返回 `DONE == '￿'`）。
- 读值：`current()`（index 在 endIndex 时返回 DONE）。
- 其他：`clone()`（extends Cloneable）、常量 `DONE`。

## 2. Java 用户如何使用
`for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) { ... }` 是经典遍历；
双向编辑（如文本编辑器光标移动）用 next/previous/setIndex 往返；`new StringCharacterIterator(s,
begin, end, pos)` 限定子区间。链路：first -> current/next 循环 -> DONE 终止 -> previous/setIndex
回退 -> getIndex/getBeginIndex/getEndIndex 定位。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/CharacterIterator.java`（本地 `~/git/jdk25u-dev`）：
  接口 javadoc 定义 first/last/current/next/previous/setIndex/getIndex/getBeginIndex/getEndIndex/clone
  及 `DONE`，说明 “previous() 越过 begin 返回 DONE、next() 越过 end 返回 DONE”——第 2 节断言对齐。
- OpenJDK `test/jdk/java/text/CharacterIterator/CharacterIteratorTest.java`：官方测试遍历
  first→next→DONE→previous 全程并校验索引。
- OpenJDK `src/java.base/share/classes/java/text/AttributedString.java`：`AttributedStringIterator`
  实现 CharacterIterator，`internalSetIndex` 越界抛 IllegalArgumentException——第 8 节经
  StringCharacterIterator 验证 setIndex 校验。
- OpenJDK `src/java.base/share/classes/java/text/BreakIterator.java#getText` 返回 CharacterIterator，
  是“以游标描述文本”的代表性用户。

## 4. 测试用例设计 + 自动化实现（CharacterIteratorTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| first/last/current | first→'a'/index0、current、last→'c'/index2 |
| next/previous+DONE | next 越界 DONE/index3/current DONE；previous 从 end 回退；previous 越 begin DONE |
| setIndex | 定位 current、next/previous 往返、索引变化 |
| begin/end/DONE | getBeginIndex/getEndIndex、DONE==0xFFFF |
| clone | 接口 clone 得独立对象、保留 index/bounds、可遍历 |
| 子区间 | StringCharacterIterator(s,1,3,1)：bounds/first/last/index |
| 空文本 | first/current 均 DONE |
| setIndex 校验 | setIndex(-1)/setIndex(len+1) 抛 IllegalArgumentException |

共 8 组场景、约 25 个 check（`testBlackBox()`）。
