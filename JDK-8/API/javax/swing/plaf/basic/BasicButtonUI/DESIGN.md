# javax.swing.plaf.basic Basic*UI 委托装卸 — DESIGN

## 1. API 功能
`BasicUIInstallTest` 聚合覆盖 basic 包 27 个可公共实例化/工厂化的 Basic*UI 委托：createUI 静态工厂、installUI/uninstallUI 幂等、尺寸契约、类型安全与 JScrollBar/JSlider 特有契约。

## 2. Java 用户如何使用
委托可脱离当前 LaF 直接驱动：`BasicButtonUI ui = new BasicButtonUI(); ui.installUI(button); ui.getPreferredSize(button)`。createUI 分两类：无状态共享单例（Button/Label/Panel…）与每组件新实例（TextField/ScrollBar/Slider…）。

## 3. 开源用法调研
- `jdk8u-dev/.../basic/BasicButtonUI.java:103`：`createUI(JComponent)` 静态工厂；installUI(c) 强转 (AbstractButton)——错组件类型 CCE 快速失败。
- `jdk8u-dev/.../basic/BasicScrollBarUI.java`：getMaximumSize 返回 (Integer.MAX_VALUE, Integer.MAX_VALUE)（ScrollBarUI 特例）；实现 LayoutManager（preferredLayoutSize/minimumLayoutSize/layoutContainer）。
- `jdk8u-dev/.../basic/BasicSliderUI.java:84`：构造器接收其 JSlider。
- OpenJDK jtreg `test/javax/swing/plaf/basic/`：装卸回归。

## 4. 测试用例设计 + 自动化实现（119 断言）
- createUI 族（27 项反射驱动）：非空 + instanceof 对应工厂类 + 返回 ComponentUI。
- 共享/每组件实例：Button/Label/Panel 两次调用同一实例；TextField/ScrollBar/Slider 每次新实例。
- 装卸幂等（EDT）：BasicButtonUI install→pref/min/max 非空→uninstall→再 install 仍可用；组件 updateUI 恢复正式委托；BasicButtonUI.installUI(JLabel) CCE。
- TextUI 族：BasicTextFieldUI install/uninstall 再 install，pref 非空（宽度随 JDK 字体度量不同：JDK-8 10 vs JDK-27 11，故只断言非空）。
- BasicScrollBarUI：maximumSize == (MAX_VALUE, MAX_VALUE)；preferred/minimumLayoutSize 非空；getSupportsAbsolutePositioning true；setSize(20,100) 后 layoutContainer 不抛。
- BasicSliderUI(JSlider)：四个方向 preferred/minimum 尺寸非空；valueForXPosition(0) 有界；再装卸可用。
- BasicInternalFrameUI/BasicDesktopIconUI：真实 JInternalFrame 上装卸不抛 + updateUI 恢复 InternalFrameUI；DesktopIcon pref 非空。内部帧装双 UI 后 pref 会栈溢出（RootLayout↔Handler 递归），故只断言装卸不抛。

## 5. 易错规范细节
- JToolBar.Separator 需先 add 到工具栏才有 BasicToolBarSeparatorUI 语义；`toolBar.add(Component)` 返回 Component 需转 JComponent。
- installUI 的组件类型检查靠强转 CCE，不是 IAE。
