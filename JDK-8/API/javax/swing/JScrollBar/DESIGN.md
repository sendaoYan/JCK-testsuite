# javax.swing.JScrollBar 测试设计

## 1. API 功能
滚动条（`extends JComponent`，implements `java.awt.Adjustable/Accessible`）：方向（VERTICAL/HORIZONTAL）、
BoundedRangeModel 区间语义（value/extent/min/max）、unit/block 增量、AdjustmentEvent 分发。

## 2. Java 用户如何使用
`new JScrollBar()` 默认竖向 0..100 extent 10 → `setValue/setValues` → `addAdjustmentListener`；
5 参构造器一次性给齐区间；`setModel` 换引擎（增量留在滚动条本地，不入模型）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JScrollBar.java`；
`DefaultBoundedRangeModel.setValues/setExtent` 源码（批量写不钳制 value、extent 钳到 max-value）。

## 4. 测试用例设计 + 易错细节
- 默认：VERTICAL / 0/10/0/100 / unit 1 / block 10 / DefaultBoundedRangeModel /
  MetalScrollBarUI / getUnitIncrement(±方向) 与字段一致。
- **setValue 钳制到 [min, max-extent]**：1000→90、-5→0；事件恰好 1 次、类型 TRACK(5)、
  source==滚动条、adjusting false。
- **增量无任何校验**：setUnitIncrement(0)、setBlockIncrement(-1) 照单全收（与值钳制对照）。
- **setValues 是原始批量 setter**：请求值 95 原样存储（value+extent>max 的"不一致态"合法），
  随后 setVisibleAmount(50) 把 extent 钳到 max-value=5。
- ctor JScrollBar(99)/setOrientation(99) IAE；方向切换不重置增量。
- 逐变更事件计数：setMinimum/setVisibleAmount/setMaximum 各 1 次。
- 模型交换：身份、0 事件；模型直接变异经滚动条转发 1 次；setModel(null) NPE。

自动化实现位于同目录 `JScrollBarTest.java`；EDT 纪律 + Metal LaF 先行。
