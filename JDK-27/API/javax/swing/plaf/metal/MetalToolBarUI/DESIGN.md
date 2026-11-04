# javax.swing.plaf.metal.MetalToolBarUI — DESIGN

## 1. API 功能
`MetalToolBarUI` 是 Metal 观感下 `JToolBar` 的 UI 委托（`extends BasicToolBarUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalToolBarUI()`、静态 `createUI(JComponent)`。
- 生命周期：`installUI(JComponent)`、`uninstallUI(JComponent)`；维护一个 `components` 静态弱引用注册表（register/unregister/findRegisteredComponentOfType）。
- 绘制（public）：`update(Graphics,JComponent)` 处理菜单栏渐变绘制；继承 `paint`。
- 边框：`createRolloverBorder()`、`createNonRolloverBorder()`、`setBorderToNonRollover(Component)` 等（protected）。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JToolBar` 自动挂载本委托。直接使用时典型链：
`JToolBar tb = new JToolBar(); tb.add(new JButton("x")); MetalToolBarUI ui = new MetalToolBarUI(); ui.installUI(tb); ui.update(g,tb); ...; ui.uninstallUI(tb);`。`update` 在 `MenuBar.gradient` 存在、水平方向且背景为 `UIResource` 时绘制渐变，否则退回父类 `update`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:306`：将 `"ToolBarUI"` 映射到 `MetalToolBarUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolBarUI.java:177`：`installUI` 注册组件并安装监听器。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolBarUI.java:309`：`update(Graphics,JComponent)` 入口。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/basic/BasicToolBarUI/bug4305622.java:27`：涉及 `MetalToolBarUI.installUI` 的闪烁回归用例。

## 4. 测试用例设计（黑盒部分，`MetalToolBarUITest.testBlackBox`）
1. 构造 `MetalToolBarUI` 非空、`createUI` 返回本类实例。
2. `installUI(tb)` 无异常；工具栏宽高为正。
3. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`update(g,tb)`、`paint(g,tb)` 均无异常。
4. `uninstallUI(tb)` 无异常。
