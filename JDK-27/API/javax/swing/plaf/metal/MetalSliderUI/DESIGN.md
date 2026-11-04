# javax.swing.plaf.metal.MetalSliderUI — DESIGN

## 1. API 功能
`MetalSliderUI` 是 Metal 观感下 `JSlider` 的 UI 委托（`extends BasicSliderUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalSliderUI()`、静态 `createUI(JComponent)`。
- 生命周期：`installUI(JComponent)`（初始化轨道宽度、刻度长度、缩略图标等 Metal 属性）。
- 绘制（public）：`paintThumb(Graphics)`、`paintTrack(Graphics)`、`paintFocus(Graphics)`。
- 查询（public）：`getTickLength()` 返回水平/垂直刻度区域高度/宽度；另有 protected `getTrackWidth()`、`getTrackLength()`、`getThumbOverhang()` 等。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JSlider` 自动挂载本委托。直接使用时典型链：
`MetalSliderUI ui = new MetalSliderUI(); ui.installUI(s); int tick = ui.getTickLength(); ui.paintTrack(g); ui.paintThumb(g); ui.paintFocus(g); ui.uninstallUI(s);`。`installUI` 会先 `calculateGeometry()` 计算 thumb/track/focus 矩形，使后续各 paint 方法可安全调用。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:301`：将 `"SliderUI"` 映射到 `MetalSliderUI`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalSliderUI/Test6657026.java:60-64`：`MetalSliderUI ui = (MetalSliderUI) slider.getUI(); int actual = ui.getTickLength();` 断言默认刻度长度为 11（验证 `getTickLength()` 的公开行为）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSliderUI.java:521`：`getTickLength()` 按 `slider.getOrientation()` 返回 `safeLength + TICK_BUFFER + 1/3`。

## 4. 测试用例设计（黑盒部分，`MetalSliderUITest.testBlackBox`）
1. 构造 `MetalSliderUI` 非空、`createUI` 返回本类实例。
2. `installUI(s)` 无异常；slider 宽高为正。
3. `getTickLength() >= 0`。
4. 分别画到 `BufferedImage`：`paintThumb(g)`、`paintTrack(g)`、`paintFocus(g)` 均无异常。
5. `uninstallUI(s)` 无异常。
