# javax.swing.plaf.basic.BasicSliderUI — DESIGN

## 1. API 功能
`SliderUI` 子类：`createUI`、构造 `BasicSliderUI(JSlider)`、install/uninstall、常量 `POSITIVE_SCROLL`/`NEGATIVE_SCROLL`/`MIN_SCROLL`/`MAX_SCROLL`、`getPreferredSize`。

## 2. Java 用户如何使用
由 LookAndFeel 通过 `UIManager.getUI(component)` 分发；测试直接 `new BasicSliderUI()` → `setUI(component)` → `getUI()`/`getPreferredSize()` → `setUI(null)`；`createUI(component)` 工厂返回同型实例。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSliderUI.java`（jdk25u-dev）。该类在 `BasicLookAndFeel.initClassDefaults` 中注册（UI 键映射到本类），由 `ComponentUI.createUI` 工厂 + `installUI`/`uninstallUI`/`getPreferredSize` 生命周期方法组成；`test/jdk/javax/swing/plaf/` 下回归用例以 `installUI` + `getPreferredSize` + `paint` 组合验证其行为。

## 4. 测试用例设计 + 自动化实现（`BasicSliderUITest.testBlackBox`）
构造组件 → `new BasicSliderUI()` → `setUI` → 断言 `getUI()==ui` → `getPreferredSize`/`getMinimumSize`/`getMaximumSize` → `setUI(null)` → `createUI` 返回同型；覆盖 install/uninstall 往返与尺寸契约。

---

## 契约深化（本轮追加，`BasicConstantsContractTest`，42 断言）

聚合 basic 包常量与小数据类：
- **BasicSliderUI 方向常量**：POSITIVE_SCROLL=+1、NEGATIVE_SCROLL=-1、MIN_SCROLL=-2、MAX_SCROLL=+2（BasicSliderUI.java:84 起）+ 全序 MIN<NEG<POS<MAX。
- **BasicScrollBarUI 高亮常量**（protected static final，经 `ProbeScrollBarUI extends BasicScrollBarUI` 静态暴露，兼容 JDK-27 强封装）：NO_HIGHLIGHT=0、DECREASE_HIGHLIGHT=1、INCREASE_HIGHLIGHT=2。
- **BasicComboBoxRenderer**：本身非 UIResource、opaque、边框非 null（no-focus 边框）；getListCellRendererComponent 返回 this 且文本=值.toString()；null 值渲染空串；UIResource 变体标记。
- **BasicComboBoxEditor**：getEditorComponent 为可编辑 JTextField；setItem/getItem 与字段文本互通；null 清空；UIResource 变体；字段 postActionEvent 恰触发一次 ActionListener。
- **BasicIconFactory**：menuArrowIcon 4x8、menuItemCheckIcon 9x9、menuItemArrowIcon 4x8，且共享单例（两次调用 ==）。
- **BasicArrowButton**：N/S/E/W 方向往返、setDirection、pref 非空；16x16 全 256 px 不透明绘制。
- **BasicInternalFrameTitlePane**：构造出子组件、pref 非空且高>0。

### 版本备注
双侧常量值一致；protected 常量访问统一走 Java 子类暴露（JDK-27 下 setAccessible 不可用）。
