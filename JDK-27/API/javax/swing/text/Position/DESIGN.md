# javax.swing.text.Position — DESIGN

## 1. API 功能
文档内“两个字符之间”位置的可追踪引用接口。仅一个方法：`getOffset()` 返回当前偏移（随文档编辑自动调整）。语义（来自 Position.java 的 javadoc）：
- 位置前插入/删除 → 偏移相应增减，保持指向同一位置。
- 删除覆盖位置的区域 → 偏移变为被删区域起点。
- 偏移 0 的位置是特例：文档编辑时永不改变。

## 2. Java 用户如何使用
```java
Position p = doc.createPosition(5);
doc.insertString(0, "abc", null);
int now = p.getOffset();   // 自动追踪
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/Position.java`：接口 javadoc 描述偏移调整规则；嵌套 `Bias` 提供 Forward/Backward。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/GapContent.java`：`createPosition(int)` 返回 `StickyPosition`，`getOffset()` 从 MarkData 按 gap 关系换算，`resetMarksAtZero` 保证偏移 0 不移动。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/AbstractDocument/6968363/Test6968363.java`：`createPosition(int) { return document.createPosition(offset); }`。

## 4. 测试用例设计 + 自动化实现（`PositionTest.testBlackBox`）
基于 `PlainDocument.createPosition`：
1. 初始偏移等于创建偏移。
2. 位置前插入 → 偏移右移；恰在位置处插入（forward bias）→ 位置越过插入文本；继续前插 → 继续右移。
3. 偏移 0 位置永不移动。
4. 文档末尾位置追踪 `getLength()` 增长。

## 5. 契约深化（本轮追加）
`testTypeShape/testCreatePosition/testPositionTracksChanges/testStartEndTrack`（22 断言）：
- 接口形状：仅 getOffset()，无字段。
- createPosition 在 0 / 中部 / 用户内容末尾（隐式换行前）分别给出精确偏移；空文档位置 0 与文档末位置。
- 追踪语义：插入点之前的位置随插入后移、之后不动；删除区间内的位置塌缩到删除起点；start/end 位置随增删精确移动（end 始终在隐式换行后）。
- Position 本身不依赖 GUI，全程 headless 验证。
