# javax.swing.JDesktopPane 测试设计（JDK-8 侧）

## 1. API 功能
MDI 桌面容器（extends JLayeredPane）：拖拽模式（LIVE=0/OUTLINE=1）、DesktopManager 装配、
getAllFrames/getAllFramesInLayer 跟踪、setSelectedFrame。

## 2. Java 用户如何使用
`JDesktopPane desk = new JDesktopPane(); desk.add(new JInternalFrame("a"));`
`desk.getAllFrames()` 遍历子框架；`desk.setDesktopManager(...)` 定制行为。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JDesktopPane.java`（getAllFrames 按 JLayeredPane layer/z-order 收集）；
JDK 测试 `test/javax/swing/JDesktopPane/`；本仓库 probe。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF + EDT）
- 拖拽常量 LIVE_DRAG_MODE==0 / OUTLINE_DRAG_MODE==1；默认 LIVE；setDragMode 往返。
- 默认 DesktopManager 非 null 且实现 DesktopManager 接口；DefaultDesktopManager 往返
  （注意 setDesktopManager(null) 不清空 getter——不断言 null）。
- 帧跟踪不变量：**getComponentCount()==getAllFrames().length**；add/remove/removeAll 同步；
  remove 后 frame.getDesktopPane()==null（解链）。
- **按显式 layer（add(f, PALETTE_LAYER)）添加仍被 getAllFrames 跟踪**，且 layer 保留。
- setSelectedFrame(null) 合法；getUIClassID "DesktopPaneUI"；Metal 下 BasicDesktopPaneUI；
  getAllFramesInLayer 空层返回空数组。

自动化实现位于同目录 `JDesktopPaneTest.java`（headless，无 @key）。
