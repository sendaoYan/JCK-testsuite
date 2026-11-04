# javax.swing.plaf.ComponentUI（UI 基类族 + 渲染层）— DESIGN

## 1. API 功能
`ComponentUI` 是所有 UI 委托的抽象基类（`ComponentUI.java:54`）。两个聚合测试文件覆盖整个 plaf 根包：
- `ComponentUIFamiliesTest`：ComponentUI 默认契约 + 30 个抽象 UI 基类（ButtonUI/LabelUI/…/ViewportUI）族形 + Metal 下组件委托 instanceof 与 updateUI 幂等 + LayerUI。
- `ComponentUIPaintContractTest`：BufferedImage 固定几何渲染断言（不依赖字体度量）。

## 2. Java 用户如何使用
外观层通常不直接调用：`JComponent.updateUI()` 会经 `UIManager.getUI(c)` 取得委托并 installUI；`getPreferredSize/getMinimumSize/getMaximumSize` 由 JComponent 尺寸方法委托；`update(g,c)` 在不透明时先填充背景再调 paint。基类默认：installUI/uninstallUI/paint 为 no-op、pref/min/max 返回 null、contains 委托组件 bounds、getBaseline 返回 -1（null NPE、负宽高 IAE）、静态 createUI 抛 `Error("ComponentUI.createUI not implemented.")`。

## 3. 开源用法调研（来源：JDK 源码 + jtreg 回归）
- `jdk8u-dev/jdk/src/share/classes/javax/swing/plaf/ComponentUI.java:263`：静态 createUI 抛 Error 的基类实现。
- `jdk8u-dev/jdk/src/share/classes/javax/swing/plaf/ComponentUI.java:186`：update() 不透明填充背景。
- `jdk8u-dev/jdk/src/share/classes/javax/swing/LayerUI.java`：LayerUI 具体类、paint 透传视图。
- OpenJDK jtreg：`test/javax/swing/UI/`（updateUI 幂等类回归）。

## 4. 测试用例设计 + 自动化实现（`ComponentUIFamiliesTest`，349 断言）
- 基类默认契约（匿名子类）：install/uninstall/paint no-op；pref/min/max null；contains 按 bounds（含 (0,0)/(49,29)/(50,30)/(-1,5) 边界）；accessible count 0/child null；getBaseline(10,10)=-1、(0,0)=-1、负宽/高 IAE、null NPE；resize behavior OTHER + null NPE；静态 createUI Error 且消息精确。
- 基类族形：30 个抽象基类 public+abstract+ComponentUI 可赋值；MenuItemUI extends ButtonUI 精确；LayerUI 具体且 Serializable。
- Metal 委托族（32 组件，EDT）：getUI() instanceof 对应基类；updateUI 两次幂等；pref/min/max 非 null 非负。JButton→MetalButtonUI、JList→BasicListUI、JTable→BasicTableUI、JInternalFrame.getDesktopIcon()→DesktopIconUI 等映射按实测锁定（两种映射类型都存在）。
- LayerUI：JLayer.getUI 同一实例、updateUI 后仍 LayerUI、paint/update/eventDispatched 不抛。

## 5. 渲染层契约（`ComponentUIPaintContractTest`，17 断言）
全部 BufferedImage headless、无字体依赖：
- ComponentUI.update：不透明精确填充背景色（全像素相等）；非不透明保持全透明。
- 不透明 JPanel：50x30 全绿精确填充。
- Metal 空文本按钮（120x60）：全像素不透明；>5 种不同颜色（渐变存在）；中心行 != 边框行；顶行 != 底行。
- BasicBorders.MenuBarBorder：恰好 80 px（40x2 底缘线）；线上方 0 px。
- MetalBorders.getButtonBorder() 环形：>0 且 <600，内部像素未画、边缘像素已画。
- BasicArrowButton EAST：16x16 全 256 px 不透明。MetalIconFactory.getCheckBoxIcon()：13x13 全画。InternalFrameCloseIcon(16)：>0。
- 不透明 JLayer + 默认 LayerUI：非空渲染。

## 6. 易错规范细节
- JComponent 无公共 getUI()（各类各自协变重暴露），聚合测试用 `comp.getClass().getMethod("getUI")` 反射统一取。
- Metal 高分按钮中心 != 自定义背景色（Metal 画主题渐变而非组件背景），只断言「不透明+多色+行间差异」级稳定不变量。
- getBaselineResizeBehavior(null) 在 JDK-8 抛 NPE（实现先判空）。
