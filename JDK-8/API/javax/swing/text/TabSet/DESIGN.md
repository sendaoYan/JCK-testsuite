# javax.swing.text.TabSet — DESIGN

## 1. API 功能
`TabSet implements Serializable`，不可变的 `TabStop` 集合（入参数组按升序排列）。方法分组：
- 构造器：`TabSet(TabStop[] tabs)`（防御性拷贝数组；null → 空集合）。
- 查询：`getTabCount()`、`getTab(int index)`（越界抛 IllegalArgumentException）。
- 定位：`getTabAfter(float location)`（返回该位置之后/处的最近 TabStop，无则 null）、`getTabIndexAfter(float location)`（返回索引，无则 -1）、`getTabIndex(TabStop tab)`（按对象身份查找，无则 -1）。
- 对象协议：`equals`（内容比较）、`hashCode`、`toString`。

## 2. Java 用户如何使用
`TabSet tabs = new TabSet(new TabStop[]{new TabStop(10f), new TabStop(20f, ALIGN_CENTER, LEAD_DOTS)});` 然后 `tabs.getTabAfter(x)` 找下一个制表位、`getTabCount()/getTab(i)` 枚举。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/ParagraphView.java`：`TabSet tabs = getTabSet(); TabStop tab = tabs.getTabAfter(x + .01f);` 再按 `tab.getAlignment()` 分支排版。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/rtf/RTFReader.java`：解析 RTF 制表位后 `new TabSet(tabs)` 组装。

## 4. 测试用例设计 + 自动化实现
`testConstructor/testGetTabBounds/testLookup/testEqualsHashCode/testToString/testSerialization`（47 断言）：
- 构造器对 null/空数组容错（getTabCount()==0）；getTabAt 越界 → ArrayIndexOutOfBoundsException。
- getTabAfter 精确语义：返回第一个 pos≥入参的 stop，无则 null；边界 pos 恰好相等也命中。
- equals/hashCode 按内容比较（同序同 stop 列表相等，个数/内容不同必不等）；null 容错。
- toString 以 "[ " 开头 " ]" 结尾并列出每个 stop。
- 序列化 round-trip：列表内容与顺序精确保留。
