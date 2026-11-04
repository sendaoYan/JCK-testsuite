# javax.swing.JRootPane — DESIGN

## 1. API 功能
窗口根容器（`extends JComponent`，implements `Accessible`），组织 contentPane/layeredPane/glassPane/menuBar。核心方法分组：
- 窗格：`getContentPane/setContentPane`、`getLayeredPane/setLayeredPane`、`getGlassPane/setGlassPane`。
- 菜单栏：`setJMenuBar/getJMenuBar`、`getMenuBar`（deprecated）。
- 默认按钮：`setDefaultButton/getDefaultButton`。
- 装饰样式：`setWindowDecorationStyle/getWindowDecorationStyle`（常量 NONE/FRAME/PLAIN_DIALOG/INFORMATION_DIALOG/ERROR_DIALOG/COLOR_CHOOSER_DIALOG/FILE_CHOOSER_DIALOG/QUESTION_DIALOG/WARNING_DIALOG；非法值 IAE）。
- 验证根：`isValidateRoot()`。

## 2. Java 用户如何使用
`JRootPane root = new JRootPane();` → `root.setContentPane(new JPanel())`（旧 contentPane 从 layeredPane 解绑、新 pane 挂到 FRAME_CONTENT_LAYER）→ `root.setJMenuBar(mb)` → `root.setDefaultButton(btn)`。窗口装饰：在已挂进窗口的 rootPane（`frame.getRootPane()`）上 `setWindowDecorationStyle(FRAME)` 循环设置各样式；`setContentPane(null)`/`setLayeredPane(null)` → IllegalComponentStateException，`setGlassPane(null)` → NPE。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JFrame.java`：`setRootPane` 把 rootPane `add(rootPane, BorderLayout.CENTER)`；JDialog/JWindow 同构。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalRootPaneUI.java`：`setWindowDecorationStyle != NONE` 时 `installClientDecorations`，经 `root.getParent()` 取窗口（独立 rootPane 会 NPE）。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JRootPane/`：窗格替换、菜单栏、装饰样式断言。

## 4. 测试用例设计（黑盒部分，`JRootPaneTest.testBlackBox`）
1. 默认 contentPane/layeredPane/glassPane/UI 非 null；contentPane 由 layeredPane 承载。
2. setContentPane 往返 + 新旧绑定切换 + null ICSE；setLayeredPane 往返 + contentPane 引用保持 + null ICSE。
3. setGlassPane 往返 + 旧可见性传播 + null NPE。
4. setJMenuBar/setMenuBar（deprecated）一致 + null 清空；setDefaultButton 往返。
5. windowDecorationStyle 默认 NONE + 非法值 -1/99 IAE（独立 rootPane 不触发装饰）；样式往返在 frame.getRootPane() 上循环断言。
6. 装入 JFrame：frameRoot isValidateRoot、contentPane 装载按钮、pack 后 componentCount==1、standalone root isValidateRoot。
