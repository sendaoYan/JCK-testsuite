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
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/rtf/RTFReader.java`：`tabAlignment = TabStop.ALIGN_LEFT; tabLeader = TabStop.LEAD_NONE; ... TabStop newStop = new TabStop(tabPosition, tabAlignment, tabLeader);` 解析 RTF 制表位。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/ParagraphView.java`：`switch (tab.getAlignment()) { case TabStop.ALIGN_LEFT: ... case TabStop.ALIGN_DECIMAL: ... }` 按对齐方式排版。

## 4. 测试用例设计 + 自动化实现
`testConstants/testConstructors/testEqualsHashCode/testToString/testSerialization`（38 断言）：
- 常量精确值：ALIGN_LEFT=0/ALIGN_RIGHT=1/ALIGN_CENTER=2/ALIGN_DECIMAL=4/ALIGN_BAR=5；LEAD_NONE=0/LEAD_DOTS=1/LEAD_HYPHENS=2/LEAD_UNDERLINE=3/LEAD_THICKLINE=4/LEAD_EQUALS=5（3 被跳过，DECIMAL 不是 3）。
- 构造器矩阵：单参默认 align=LEFT、leader=LEAD_NONE；三参 getPosition/getAlignment/getLeader 回读。
- 无校验反例：负 position、越界 alignment/leader 原样存储；Float.NaN 位置也被接受。
- equals/hashCode：位置+对齐+leader 全字段参与，任一不同即不等；自反/非 null/异类比较。
- toString 含 "tab @10.0" 位置文本，非默认对齐名与 leader 出现 "w/leaders" 标记。
- 序列化 round-trip：三字段精确保留。

### 易错规范细节
- equals 与 hashCode 必须成对使用同一组字段；TabStop 是少数实现值语义的 Swing 文本类。
