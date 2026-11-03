# javax.swing.plaf.basic.BasicSliderUI — DESIGN

## 1. API 功能
`SliderUI` 子类：`createUI`、构造 `BasicSliderUI(JSlider)`、install/uninstall、常量 `POSITIVE_SCROLL`/`NEGATIVE_SCROLL`/`MIN_SCROLL`/`MAX_SCROLL`、`getPreferredSize`。

## 2. Java 用户如何使用
由 LookAndFeel 通过 `UIManager.getUI(component)` 分发；测试直接 `new BasicSliderUI()` → `setUI(component)` → `getUI()`/`getPreferredSize()` → `setUI(null)`；`createUI(component)` 工厂返回同型实例。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSliderUI.java`（jdk25u-dev）。该类在 `BasicLookAndFeel.initClassDefaults` 中注册（UI 键映射到本类），由 `ComponentUI.createUI` 工厂 + `installUI`/`uninstallUI`/`getPreferredSize` 生命周期方法组成；`test/jdk/javax/swing/plaf/` 下回归用例以 `installUI` + `getPreferredSize` + `paint` 组合验证其行为。

## 4. 测试用例设计 + 自动化实现（`BasicSliderUITest.testBlackBox`）
构造组件 → `new BasicSliderUI()` → `setUI` → 断言 `getUI()==ui` → `getPreferredSize`/`getMinimumSize`/`getMaximumSize` → `setUI(null)` → `createUI` 返回同型；覆盖 install/uninstall 往返与尺寸契约。
