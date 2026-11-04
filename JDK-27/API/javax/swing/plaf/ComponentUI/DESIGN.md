# javax.swing.plaf.ComponentUI

## 1. API 功能

Swing 外观（Look & Feel）委托抽象基类，为具体外观实现定义组件级绘制与行为契约。

## 2. Java 用户如何使用

通过组件的 getUI() 获取，或由具体外观的 createUI(Component) 工厂方法创建并 installUI/uninstallUI。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/ComponentUI.java:54`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/ComponentUI.java:59`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：在 EDT 上 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，headful 用例在事件分发线程（EDT）上运行，结束后释放组件。

---

## 契约深化（本轮追加，`ComponentUIFamiliesTest` + `ComponentUIPaintContractTest`）

新增两个聚合测试（headless，Metal 为参考外观）：

### ComponentUIFamiliesTest（349 断言）
- **基类默认契约**（匿名 `new ComponentUI() {}`）：installUI/uninstallUI/paint 为 no-op；getPreferredSize/MinimumSize/MaximumSize 返回 null；contains 按 c.contains 语义（(0,0)/(49,29) true、(50,30)/(-1,5) false）；getAccessibleChildrenCount 0、getAccessibleChild null；getBaseline(10,10)/(0,0) 均 -1、负宽/高 IAE、null NPE；getBaselineResizeBehavior OTHER、null NPE；静态 `ComponentUI.createUI(c)` 抛 `Error`，消息精确为 `"ComponentUI.createUI not implemented."`（ComponentUI.java:268）。
- **update() 填充语义**（并见渲染测试）：不透明组件被精确填充背景色，非不透明保持全透明。
- **30 个抽象 UI 基类族形**：全部 public abstract 且 ComponentUI 可赋值；MenuItemUI 直接父类是 ButtonUI；LayerUI 具体类 + Serializable。
- **Metal 委托族**（EDT，32 个组件）：getUI() instanceof 对应基类（反射统一调用，因 getUI 非 JComponent 公共方法）；updateUI 两次幂等；pref/min/max 非 null 非负。
- **LayerUI**：JLayer.getUI 同一实例、updateUI 后仍 LayerUI、paint/update/eventDispatched 不抛。

### ComponentUIPaintContractTest（17 断言，BufferedImage 固定几何）
- ComponentUI.update：不透明 20x10 全像素 == 0xFFFF0000；非不透明 0 px。
- 不透明 JPanel 50x30 全绿精确填充。
- Metal 空文本按钮 120x60：全不透明、>5 种颜色（渐变）、中心 != 顶/底行。
- BasicBorders.MenuBarBorder 40x20 恰 80 px 且全在底两行；MetalBorders.getButtonBorder() 环形（边缘画、内部 (15,10) 不画）。
- BasicArrowButton EAST 16x16 全 256 px；MetalIconFactory.getCheckBoxIcon() 13x13 全 169 px；getInternalFrameCloseIcon(16) >0（paintIcon 传 JButton）。
- 不透明 JLayer + 默认 LayerUI 渲染非空。

### 版本备注（javap 逐包核对）
- javax.swing.plaf 根包双侧类型面一致（SynthIcon 属 synth 包，见该包 DESIGN）。
- JDK-27 源码锚点：`~/git/jdk27/src/java.desktop/share/classes/javax/swing/plaf/ComponentUI.java:268`。
