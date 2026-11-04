# javax.swing.SizeRequirements 测试设计

## 1. API 功能
LayoutManager2 尺寸协商的纯数据+算法类：公开 minimum/preferred/maximum/alignment 四字段，
getTiled/AlignedSizeRequirements 聚合子项需求，calculateTiled/AlignedPositions 把总量分配成
offsets/spans 数组；BoxLayout/OverlayLayout 内部即用它求解。

## 2. Java 用户如何使用
自定义 LayoutManager2 时 `SizeRequirements.getTiledSizeRequirements(children)` 求总需求，
`calculateTiledPositions(allocated, total, children, offsets, spans)` 求每个子项的实际区间。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/SizeRequirements.java`（adjustSizes 历史空实现）；
BoxLayout.java/OverlayLayout.java 的调用点。

## 4. 测试用例设计 + 易错细节
- 默认构造 (0,0,0,0.5)；toString 精确 "[3,5,7]@0.5"。
- tiled 聚合 = 逐值求和：{3/5/7}×3 → 9/15/21；{10, 4/8/16} → 14/18/26。
- aligned 聚合覆盖"最左对齐左缘到最右对齐右缘"：{3/5/7}@{0,.5,1} → 6/10/14@0.5；
  **单个子项 alignment 变 1/3**（0.5*7=3.5 → 3，除以自身 extent 7 的归一化），不是透传 0.5；
  大 minimum 子项主导：{10@.5, 4/8/16@.5} → 10/10/16@.5。
- **calculateTiledPositions 对 min=pref=max 的刚性子项无视 total**：20/14/10/8 全给 [0,7][7,7]；
  可收缩 {5/7/7} 在 10 → [0,5][5,5]；可增长 {7/7/9} 在 16 → [0,8][8,8]；
  {10 定死, 10/20/30} 在 30 → 富余全给弹性项 [10,20]。
- calculateAlignedPositions 反向对齐 {7@0,7@1} 在 30 → offs [15,8]；{10@0,10@1} 在 20 → [10,0]。
- **adjustSizes 是历史桩：恒返回空数组**（分配逻辑实际在 calculateTiledPositions）。

自动化实现位于同目录 `*Test.java`。
