# javax.swing.text.Segment — DESIGN

## 1. API 功能
`Segment implements Cloneable, CharacterIterator, CharSequence`，是字符数组片段的视图（避免拷贝），直接暴露 `array/offset/count` 三个公有字段。方法分组：
- 构造器：`Segment()`（空）、`Segment(char[] array, int offset, int count)`。
- CharSequence：`charAt(int)`、`length()`、`subSequence(int,int)`、`toString()`。
- CharacterIterator：`first()/last()/current()/next()/previous()/setIndex(int)/getBeginIndex()/getEndIndex()/getIndex()`。
- 其他：`setPartialReturn(boolean)/isPartialReturn()`、`clone()`。

## 2. Java 用户如何使用
`Segment seg = new Segment(); doc.getText(offs, len, seg);` 后直接读 `seg.array/offset/count` 做零拷贝遍历；或 `seg.toString()` 取字符串；用 `first()/next()` 按 CharacterIterator 协议遍历，`setIndex(pos)` 定位。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java`：`getText(int offset, int length, Segment txt)` 通过 GapContent 把文本片段填入 Segment；另有 `Segment seg = SegmentCache.getSharedSegment(); ... seg.next()` 遍历。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/Utilities.java`：`getWordStart/getWordEnd` 用 `Segment` 承载段落文本再交给 BreakIterator。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/PlainView.java`：绘制时用 Segment 引用文档文本避免拷贝。

## 4. 测试用例设计（黑盒部分，`SegmentTest.testBlackBox`）
数组 `"abcdefgh"` 切片 `[2,6)`：
1. 字段直读 array/offset/count + `toString()=="cdef"` + length/begin/end/charAt/subSequence。
2. CharacterIterator 导航：first/last/current/setIndex/next/previous + 边界 DONE。
3. `setPartialReturn` 往返；`clone()` 独立对象且视图一致。
4. 空 Segment（默认构造）各查询返回 0/空串/DONE。
5. 异常：charAt/subSequence 越界 → StringIndexOutOfBoundsException；setIndex 越界 → IllegalArgumentException。

## 5. 契约深化（本轮追加）
用例重组为 `testDefaultConstructor/testArrayConstructor/testCharSequence/testCharacterIterator/testPartialReturn/testClone`（71 断言）：
- 构造器矩阵：默认构造为全零空片段；数组构造器后 `array/offset/count` 字段直读。
- CharSequence 契约与 String 语义一致（length/charAt/subSequence/toString）；charAt/subSequence 越界 → StringIndexOutOfBoundsException。
- CharacterIterator 全协议：first/last/current/next/previous/setIndex，两端各返回一次 DONE 后保持 DONE；getBeginIndex/getEndIndex/getIndex 精确回读。
- setIndex 越界（<begin 或 >end）→ IllegalArgumentException。
- setPartialReturn 默认 false，切换后读回；clone() 为独立副本且视图一致。
