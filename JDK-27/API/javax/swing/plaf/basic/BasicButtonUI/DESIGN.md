# javax.swing.plaf.basic.BasicButtonUI

## 1. API 功能

Swing 基础（Basic）外观委托类，负责把对应的 Swing 组件的绘制、布局与输入行为具体化。

## 2. Java 用户如何使用

通过组件的 getUI() 获取，或由具体外观的 createUI(Component) 工厂方法创建并 installUI/uninstallUI。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicButtonUI.java:69`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicButtonUI.java:90`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：在 EDT 上 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，headful 用例在事件分发线程（EDT）上运行，结束后释放组件。

---

## 契约深化（本轮追加，`BasicUIInstallTest`，119 断言）

聚合 basic 包 27 个委托的 createUI/装卸契约（EDT、headless）：
- **createUI 族**（27 项反射 `factory.getMethod("createUI", JComponent.class)`）：非空 + instanceof 对应工厂类。
- **共享 vs 每组件实例**：Button/Label/Panel UI 两次调用同一实例；TextField/ScrollBar/Slider 每次新实例。
- **装卸幂等**：BasicButtonUI（pref/min/max 非 null、getDefaultTextIconGap>=0）、BasicLabelUI、BasicTextFieldUI（instanceof TextUI）、BasicScrollBarUI、BasicSliderUI 均 install→uninstall→re-install 可用；组件 updateUI 恢复正式委托。
- **类型安全反例**：`new BasicButtonUI().installUI(new JLabel(...))` → ClassCastException（installUI 强转 AbstractButton）。
- **BasicScrollBarUI 特有**：getMaximumSize == (Integer.MAX_VALUE, Integer.MAX_VALUE)；preferredLayoutSize/minimumLayoutSize 非空；getSupportsAbsolutePositioning true；setSize(20,100) 后 layoutContainer 不抛。
- **BasicSliderUI**：构造器收 JSlider；四方向 preferred/minimum 尺寸非空；valueForXPosition(0) 在 min..max 内。
- **BasicInternalFrameUI / BasicDesktopIconUI**：真实 JInternalFrame 上装卸不抛 + updateUI 恢复；DesktopIcon pref 非空（内部帧双装后 getPreferredSize 会 StackOverflowError，故不断言其尺寸）。

### 版本备注
BasicTextFieldUI 空字段 pref 宽 JDK-8 为 10、JDK-27 为 11（字体度量差异），只断言非空；其余行为双侧一致。
