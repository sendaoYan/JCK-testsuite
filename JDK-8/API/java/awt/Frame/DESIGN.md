# java.awt.Frame 黑盒行为测试设计（JDK-8）

## 1. API 功能
带标题顶层窗（extends Window，implements MenuContainer）：4 构造器（null gc 允许）、setTitle/getTitle
（null→""）、setUndecorated/isUndecorated、setResizable、setMenuBar/getMenuBar、setIconImage/getIconImage、
状态机常量 NORMAL=0/ICONIFIED=1/MAXIMIZED_HORIZ=2/MAXIMIZED_VERT=4/MAXIMIZED_BOTH=6 与
getState/setState/getExtendedState/setExtendedState、setMaximizedBounds、弃用 setCursor(int)/getCursorType、
getFrames、pack。

## 2. Java 用户如何使用
`Frame f = new Frame("t"); f.setMenuBar(mb); f.setVisible(true);`
`f.addPropertyChangeListener(...)` 观察 title PCE；状态机按 `Toolkit.isFrameStateSupported` 条件切换。

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Frame.java`
（setMenuBar 触发旧栏 removeNotify；setUndecorated peer 存在时抛 ICSE；getFrames 过滤 Window.getWindows；
setExtendedState 不支持状态直接 return 不存储）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集）→ `testStructure`。
- 常量精确值 + 4 构造器 + 默认 BorderLayout + title/resizable/undecorated/menubar/icon/maximizedBounds/
  弃用光标类型往返 + setTitle(null)→"" + 显示路径 getFrames/dispose → `testBlackBox`。
- 标题 PCE 精确载荷 `title||A`/`title|A|B`/`title|B|`、同值短路 → `testTitlePropertyChange`。
- 图标列表语义：默认空、源列表拷贝、返回列表拷贝、getIconImage==首个、setIconImage 单元素、空列表清空
  （**易错**：setIconImages 声明在 Window，getIconImage 在 Frame）→ `testIconListSemantics`。
- 状态机：ICONIFIED 条件往返、setState(88) 掩码保持 NORMAL、
  **setExtendedState(不支持) 静默不存储（锁 else 分支 NORMAL）**、组合态条件往返 → `testStateContracts`。
- pack 非负尺寸 + insets 非空 → `testPack`。
- 反例：displayable 下 `setUndecorated(true)`→IllegalComponentStateException。
- JDK-8 侧 89 断言通过（DISPLAY=:77，@key headful）。
