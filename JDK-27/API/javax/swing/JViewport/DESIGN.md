# javax.swing.JViewport 测试设计

## 1. API 功能
视口（JComponent + Accessible，Scrollable）：显示大视图的可视窗口，维护
view/extent（可视范围）/viewPosition（滚动偏移）三元组；三种滚动模式
SIMPLE=0/BLIT=1/BACKINGSTORE=2（默认 BLIT）；toViewCoordinates 坐标换算；
isOptimizedDrawingEnabled==false（子件可覆盖）。

## 2. Java 用户如何使用
`JViewport vp = new JViewport(); vp.setView(bigPanel); vp.setViewPosition(new Point(x, y));`
一般不直接用——JScrollPane 内部持有它；自绘滚动容器（地图/图表）会直接持有。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JViewport.java`；
JScrollPane/ViewportLayout 的协作；`test/jdk/javax/swing/JViewport/`。

## 4. 测试用例设计 + 易错细节
- 默认契约：view null/viewSize 0,0/viewPosition 0,0/extentSize 0,0/viewRect
  (0,0,0,0)/scrollMode BLIT/isOptimizedDrawingEnabled false/insets 全 0/
  getUIClassID=="ViewportUI"/Metal→BasicViewportUI/border null。
- 模式矩阵：SIMPLE/BLIT/BACKINGSTORE 常量 0/1/2 与 setter 往返；
  废弃 API setBackingStoreEnabled(true/false) 等价映射到 BACKINGSTORE/BLIT、
  isBackingStoreEnabled 同步。
- view 接线：setView(view) 后 **view.getParent()==viewport**；setView(null) 合法。
- 尺寸/位置：setViewSize/setExtentSize/setViewPosition 往返；
  **未显示时 viewPosition 不被钳位**（(500,500) 原样保留——与 JScrollBar 的
  setValue 钳位行为形成对照）；getViewRect()==new Rectangle(pos, extent) 组合式。
- 坐标换算：toViewCoordinates(Point/Dimension) 恒等映射（基础实现）。
- **setBorder 反例**：setBorder(null) 合法，setBorder(非 null)→IllegalArgumentException
  （"JViewport.setBorder() not supported"，断类型即可）；拒绝后 border 仍 null。
- scrollRectToVisible：无 view/未显示均 no-throw（空操作或位置平移）。
- 环境：headless 下 Metal LaF 先装；EDT 操作。

自动化实现位于同目录 `JViewportTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印
`PASS (N assertions)`；jtreg `@run main` 直接驱动。
