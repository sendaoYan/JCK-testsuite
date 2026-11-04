# javax.swing.JScrollPane 测试设计

## 1. API 功能
滚动容器（JComponent + ScrollPaneConstants + Accessible）：组合 viewport（JViewport）+
垂直/水平 JScrollBar + 行/列头 viewport + 四角组件；滚动条策略
VSB 20/21/22、HSB 30/31/32（AS_NEEDED/NEVER/ALWAYS，默认 AS_NEEDED）；
布局委托 ScrollPaneLayout（UIResource 子类）；Metal→MetalScrollPaneUI。

## 2. Java 用户如何使用
`JScrollPane sp = new JScrollPane(textArea); sp.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);`
表格/树/文本区标准包裹；`sp.setRowHeaderView(header)` 做行号列。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JScrollPane.java` 与
`ScrollPaneLayout.java`；`test/jdk/javax/swing/JScrollPane/`。

## 4. 测试用例设计 + 易错细节
- 常量矩阵：VERTICAL_SCROLLBAR_AS_NEEDED=20/NEVER=21/ALWAYS=22；
  HORIZONTAL 30/31/32（注意 20 与 30 不连续段易混）。
- 默认契约：双策略 AS_NEEDED；viewport 非 null 且 view 为 null；两条滚动条非 null；
  wheelScrolling true；getUIClassID=="ScrollPaneUI"；Metal→MetalScrollPaneUI。
- **表头懒创建**：未 setColumnHeaderView/setRowHeaderView 前
  getColumnHeader()/getRowHeader()==null（布局用不到就不建——与滚动条"总是建好"
  的行为相反，成对断言）。
- 布局接线：pane.getLayout() instanceof ScrollPaneLayout 且是 UIResource；
  viewport.getLayout() instanceof ViewportLayout。
- 策略反例：setVertical/HorizontalScrollBarPolicy(99)→IllegalArgumentException
  （断类型）；策略 setter 全矩阵往返；wheel 开关往返。
- corner：setCorner(LOWER_LEFT_CORNER, c) 往返；**setCorner(key,null) 清空**；
  getCorner(未知 key)==null 不抛；setCorner(未知 key, c)→IAE。
- 部件替换：setViewport/setVerticalScrollBar/setHorizontalScrollBar/setViewportBorder
  恒等往返（自定义 viewport 的 view 保留）；setViewportView 走 viewport；
  嵌套 scroll pane 合法。
- 固定几何（Metal 度量，双侧实测一致）：100x100 pane + 400x400 固定 view +
  vsb ALWAYS → viewport bounds==(1,1,82,82)、vsb bounds==(83,1,15,82) 且可见。
  **不要在带表头的场景断像素**——表头尺寸来自 JLabel 字体度量，不稳定；
  表头场景改用 setPreferredSize 固定后再断。
- 环境：headless 下 Metal LaF 先装；EDT 操作。

自动化实现位于同目录 `JScrollPaneTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印
`PASS (N assertions)`；jtreg `@run main` 直接驱动。
