# javax.swing.JLayeredPane 测试设计

## 1. API 功能
分层容器（JComponent + Accessible）：子组件按 Integer 层值组织，
**child 数组按层值降序维护**（层高在前/画在最上层），同层内保持插入顺序；
提供层内 position 操作（moveToFront/moveToBack/setPosition）、层查询
（getComponentCountInLayer/getComponentsInLayer/highestLayer/lowestLayer）、
静态助手（getLayer/putLayer/getLayeredPaneAbove）。固定布局（layout==null）。

## 2. Java 用户如何使用
`JLayeredPane pane = new JLayeredPane(); pane.add(dialog, JLayeredPane.MODAL_LAYER);`
桌面式叠放（弹窗层>拖拽层>调色板层>内容层）；JRootPane 内部即用它承载
contentPane/glassPane（FRAME_CONTENT_LAYER=-30000）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JLayeredPane.java`；
JRootPane/JInternalFrame 的使用；`test/jdk/javax/swing/JLayeredPane/`。

## 4. 测试用例设计 + 易错细节
- 层常量：DEFAULT=0/PALETTE=100/MODAL=200/POPUP=300/DRAG=400/FRAME_CONTENT=-30000；
  LAYER_PROPERTY=="layeredContainerLayer"。
- **add 约束歧义（经典坑）**：`add(comp, 5)`（int）把 5 当**索引**，层仍是默认 0；
  必须用 `add(comp, Integer.valueOf(5))` 才是层。两者混合后层查询/索引会误导，
  测试用混合场景锁定（int 加的件 lowestLayer 兜底 0）。
- 排序契约：加入 (5,5,3) 层三件后 child 顺序 [5层先加, 5层后加, 3层]；
  setLayer(b,7) 提层后 b 立即回到 index 0；**同层内 moveToFront/moveToBack
  只在本层内换位**（不动其他层的件）。
- 空 pane：highestLayer/lowestLayer==0、getComponentCountInLayer==0、
  getComponentsInLayer 长度 0、getIndexOf(非子件)==-1、
  静态 getLayer(未挂载组件)==0（DEFAULT 兜底）。
- 移除：remove 后 getIndexOf==-1 但 **getLayer(该件) 保留原层值**（client 属性仍在）；
  removeAll 后 highest/lowest 归 0、getLayeredPaneAbove 也不再找到它。
- 静态助手：putLayer/getLayer 往返（无需挂载）；getLayeredPaneAbove 沿父链找
  最近 JLayeredPane，独立组件返回 null。
- 环境：headless 下 Metal LaF 先装；EDT 操作；JDK-27 特有：基类
  `JComponent.getUI()` 存在且对 JLayeredPane 返回 null（JDK-8 无该基类方法）。

自动化实现位于同目录 `JLayeredPaneTest.java`：`check(boolean,msg)` 计数断言，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
