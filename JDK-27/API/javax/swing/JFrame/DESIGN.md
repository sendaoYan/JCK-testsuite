# javax.swing.JFrame — DESIGN

## 1. API 功能
顶层 Swing 窗口（`extends java.awt.Frame`，implements `WindowConstants/Accessible/RootPaneContainer/TransferHandler$HasGetTransferHandler`）。核心方法分组：
- 构造器：`JFrame()`、`JFrame(String title)`。
- 关闭操作：`setDefaultCloseOperation(int)/getDefaultCloseOperation()`（常量 EXIT_ON_CLOSE=3、HIDE_ON_CLOSE、DISPOSE_ON_CLOSE、DO_NOTHING_ON_CLOSE）。
- 根窗格（RootPaneContainer）：`getRootPane()/setRootPane(protected)/getContentPane()/setContentPane()/getLayeredPane()/setLayeredPane()/getGlassPane()/setGlassPane()`。
- 菜单栏：`setJMenuBar(JMenuBar)/getJMenuBar()`。
- 图标：`setIconImage(Image)/getIconImages()`。
- 继承：`setTitle/getTitle/setSize/pack/setVisible/isDisplayable/isVisible`、`add/remove/setLayout`（委托给 contentPane）。

## 2. Java 用户如何使用
`new JFrame("标题")` → `setDefaultCloseOperation(EXIT_ON_CLOSE)` → `getContentPane().add(new JButton(...))` → `setJMenuBar(menuBar)` → `setIconImage(img)` → `pack()`/`setSize(w,h)` → `setVisible(true)` → 窗口生命周期结束 `dispose()`。add/remove/setLayout 会自动落到 contentPane；`setContentPane(null)`/`setLayeredPane(null)` 抛 `IllegalComponentStateException`，`setGlassPane(null)` 抛 NPE。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk，WebSearch 不可靠时按约定改用）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JOptionPane.java`：`createDialog` 内部 `new JDialog((Frame)getFrameForComponent(...), ...)` 作为父窗口；`JRootPane.setWindowDecorationStyle` 依赖 JFrame 的 rootPane 装饰。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JFrame/JFrameRepaint.java`、`MetalBorders/...`：直接 `new JFrame()` → `getContentPane().add` → `setVisible(true)` 驱动重绘测试。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SwingUtilities.java`：`getWindowAncestor` 依赖窗口层级。

## 4. 测试用例设计（黑盒部分，`JFrameTest.testBlackBox`）
1. defaultCloseOperation 默认 HIDE_ON_CLOSE + 四常量往返 + 非法值（7、-1）→ IAE 且状态不变。
2. rootPane 非 null、稳定实例、instanceof JRootPane。
3. title 默认 ""、setTitle 往返、`new JFrame(String)` 构造标题。
4. iconImages 默认空、setIconImage 后 1 元素列表。
5. add/setLayout/remove 落到 contentPane（计数/布局/parent 断言）；setContentPane 替换往返 + null → ICSE。
6. setJMenuBar 往返 + null。
7. layeredPane/glassPane 链：setLayeredPane 往返、null → ICSE；setGlassPane 往返、null → NPE、替换 glassPane 继承不可见。
8. pack + setSize(300,200) 往返 + setVisible(true/false) 往返；dispose 后 !isDisplayable。
