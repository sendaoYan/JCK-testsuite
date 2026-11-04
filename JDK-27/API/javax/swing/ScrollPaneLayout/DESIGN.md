# javax.swing.ScrollPaneLayout 测试设计

## 1. API 功能
JScrollPane 的布局管理器（LayoutManager + ScrollPaneConstants + Serializable）：
管理 viewport/vsb/hsb/行头/列头/四角九个槽位；`syncWithScrollPane` 从 pane 抓取
九个部件引用；按 VSB/HSB 策略计算各槽位几何；UI 安装的是其 UIResource 子类。

## 2. Java 用户如何使用
一般不直接用（JScrollPane 默认安装）。自定义布局时：
`ScrollPaneLayout lm = new ScrollPaneLayout(); sp.setLayout(lm); lm.syncWithScrollPane(sp);`
或子类覆写 layoutContainer 定制角/头布局。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/ScrollPaneLayout.java`；
JScrollPane.updateUI 里 ScrollPaneLayout$UIResource 的安装路径。

## 4. 测试用例设计 + 易错细节
- 策略：set/getVertical/HorizontalScrollBarPolicy 往返；**setPolicy(99)→
  IllegalArgumentException（垂直/水平两个变体都断）**。
- **addLayoutComponent 按 String key**：未知 key（"x"）→IllegalArgumentException
  （"invalid layout key x"，断类型）；合法 key（LOWER_LEFT_CORNER 等）挂角组件，
  getCorner 可取回；removeLayoutComponent 任意组件 no-throw。
  （注意：ScrollPaneLayout 只实现 LayoutManager（String 版 add），与
  OverlayLayout 等 LayoutManager2 的 AWTError 行为不同。）
- syncWithScrollPane 恒等接线：layout.getViewport()==sp.getViewport()、
  vsb/hsb 同；普通 pane 的 row/column header 与 corner 在 layout 侧为 null；
  pane 上 setColumnHeaderView 之后再 fresh sync 能拿到 columnHeader。
- 尺寸：preferredLayoutSize 非 null；minimumLayoutSize==22x22（Metal 度量，
  双侧实测一致：1px 边框+滚动条最小值）。
- 空 pane 布局（AS_NEEDED 默认策略）：100x100 pane + 空 view →
  viewport==(1,1,97,97)、两滚动条都不可见（视口占满内区，无内容不显示滚动条）。
- 环境：headless 下 Metal LaF 先装；组件操作 EDT。

自动化实现位于同目录 `ScrollPaneLayoutTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印
`PASS (N assertions)`；jtreg `@run main` 直接驱动。
