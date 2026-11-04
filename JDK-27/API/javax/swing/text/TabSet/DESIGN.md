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
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java`：`TabSet tabs = getTabSet(); TabStop tab = tabs.getTabAfter(x + .01f);` 再按 `tab.getAlignment()` 分支排版。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/rtf/RTFReader.java`：解析 RTF 制表位后 `new TabSet(tabs)` 组装。

## 4. 测试用例设计（黑盒部分，`TabSetTest.testBlackBox`）
1. `getTabCount/getTab` 数量与对象身份；构造器对入参数组做防御性拷贝（改原数组不影响）。
2. `getTabAfter/getTabIndexAfter` 在 5/15/25 处的索引/对象（含 null/-1 边界）。
3. `getTabIndex` 按身份而非 equals（`new TabStop(10f)` 返回 -1）。
4. `getTab(-1)/getTab(2)` → IllegalArgumentException。
5. equals（自反/对称/不同数量/不同元素/null/非 TabSet）+ 相等对象 hashCode 相等 + toString。
6. 空集合（null 入参）各查询行为。
