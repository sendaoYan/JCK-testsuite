# javax.swing.text.TabStop — DESIGN

## 1. API 功能
`TabStop implements Serializable`，不可变的单个制表位（RTF 语义）。常量与查询分组：
- 对齐常量：`ALIGN_LEFT=0/ALIGN_RIGHT=1/ALIGN_CENTER=2/ALIGN_DECIMAL=4/ALIGN_BAR=5`。
- 引导常量：`LEAD_NONE=0/LEAD_DOTS=1/LEAD_HYPHENS=2/LEAD_UNDERLINE=3/LEAD_THICKLINE=4/LEAD_EQUALS=5`。
- 构造器：`TabStop(float pos)`（默认 ALIGN_LEFT/LEAD_NONE）、`TabStop(float pos, int align, int leader)`。
- 查询：`getPosition()`、`getAlignment()`、`getLeader()`。
- 对象协议：`equals`（位置/对齐/引导三者相等）、`hashCode`（`align^leader^round(pos)`）、`toString`。

## 2. Java 用户如何使用
`new TabStop(100f)` 表示左对齐无引导的 100 单位制表位；`new TabStop(100f, TabStop.ALIGN_CENTER, TabStop.LEAD_DOTS)` 表示居中且带点引导的制表位；多个放入 `TabSet` 供段落视图排版。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/rtf/RTFReader.java`：`tabAlignment = TabStop.ALIGN_LEFT; tabLeader = TabStop.LEAD_NONE; ... TabStop newStop = new TabStop(tabPosition, tabAlignment, tabLeader);` 解析 RTF 制表位。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ParagraphView.java`：`switch (tab.getAlignment()) { case TabStop.ALIGN_LEFT: ... case TabStop.ALIGN_DECIMAL: ... }` 按对齐方式排版。

## 4. 测试用例设计（黑盒部分，`TabStopTest.testBlackBox`）
1. 全部对齐/引导常量数值。
2. 单参构造默认 ALIGN_LEFT/LEAD_NONE；全参构造对齐/引导/位置精确往返。
3. equals：自反、对称、位置/对齐/引导任一不同则不相等、null/非 TabStop 不相等。
4. hashCode：相等对象相等 + 具体公式值（`TabStop(10f).hashCode()==10`）。
5. toString 各对齐/引导组合输出。
