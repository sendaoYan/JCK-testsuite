# javax.swing.JWindow — DESIGN

## 1. API 功能
无边框顶层窗口（`extends java.awt.Window`，implements `Accessible/RootPaneContainer/TransferHandler$HasGetTransferHandler`）。核心方法分组：
- 构造器：`JWindow()`、`JWindow(Window owner)`、`JWindow(Frame)`、`JWindow(Window, GraphicsConfiguration)`。
- 窗格链：`getRootPane`、`getContentPane/setContentPane`、`getLayeredPane/setLayeredPane`、`getGlassPane/setGlassPane`。
- 几何：`setLocation/getX/getY`、`setSize/getWidth/getHeight`、`setBounds`。
- 可见性：`setVisible/isVisible`（JWindow 默认不可见）。
- 监听器：`addComponentListener/getComponentListeners/removeComponentListener`（继承）。

## 2. Java 用户如何使用
`new JWindow(owner)`（有父窗口）或 `new JWindow()`（自动共享隐藏 owner Frame）→ `getContentPane().add(comp)` → `setLocation(x,y)`/`setSize(w,h)`/`setBounds(...)` → `setVisible(true/false)`。`setContentPane(null)`/`setLayeredPane(null)` → IllegalComponentStateException。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JWindow.java`：构造经 `SwingUtilities.getSharedOwnerFrame()` 提供共享 owner；RootPaneContainer 语义与 JFrame 一致。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JWindow/`：owner 保留、窗格替换、几何往返断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Popup.java`：轻量/重量级弹窗内部使用 JWindow。

## 4. 测试用例设计（黑盒部分，`JWindowTest.testBlackBox`）
1. owner 保留（Window ctor）+ no-arg ctor 共享 owner 非 null。
2. rootPane/panes 非 null + 稳定实例 + instanceof。
3. add 落到 contentPane（计数/parent）+ remove；setContentPane 往返 + null ICSE。
4. setLayeredPane/setGlassPane 往返 + null 拒绝 + 替换 glassPane 不可见。
5. setLocation/setSize/setBounds 往返；setVisible(false) 保持隐藏。
6. ComponentListener 注册/移除计数；dispose 后全部窗口 !isDisplayable。
