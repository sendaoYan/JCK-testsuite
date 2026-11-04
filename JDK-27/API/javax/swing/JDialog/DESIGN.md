# javax.swing.JDialog — DESIGN

## 1. API 功能
对话框窗口（`extends java.awt.Dialog`，implements `WindowConstants/Accessible/RootPaneContainer/TransferHandler$HasGetTransferHandler`）。核心方法分组：
- 构造器：`JDialog()`、`JDialog(Frame)`、`JDialog(Frame, boolean)`、`JDialog(Frame, String)`、`JDialog(Frame, String, boolean)`、`JDialog(Dialog/Window, ...)`。
- 模态：`isModal()/getModalityType()`（继承）。
- 关闭操作：`setDefaultCloseOperation/getDefaultCloseOperation`（JDialog 拒绝 EXIT_ON_CLOSE → IAE）。
- 窗格链：`getContentPane/setContentPane/getLayeredPane/setLayeredPane/getGlassPane/setGlassPane/getRootPane`。
- 菜单栏：`setJMenuBar/getJMenuBar`；标题 `setTitle/getTitle`、`setResizable/isResizable`。

## 2. Java 用户如何使用
`new JDialog(owner, "标题", true)`（模态）或 `new JDialog()`（modeless）→ `setDefaultCloseOperation(DISPOSE_ON_CLOSE)` → `setContentPane(new JPanel(...))` → `setJMenuBar(mb)` → `setSize(w,h)` → `setVisible(true/false)`。`setContentPane(null)`/`setLayeredPane(null)` → IllegalComponentStateException。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JOptionPane.java`：`createDialog` 用 `new JDialog((Dialog)parent, title, true)` 模态对话框。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JDialog/`：模态类型、contentPane 委托、默认关闭操作断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JFrame.java`：JDialog 与 JFrame 共享 RootPaneContainer 语义。

## 4. 测试用例设计（黑盒部分，`JDialogTest.testBlackBox`）
1. no-arg 对话框 modeless、模态构造器 APPLICATION_MODAL、owner 保留。
2. defaultCloseOperation 默认 HIDE_ON_CLOSE + 往返 + EXIT_ON_CLOSE 拒绝 IAE + 非法值 42 IAE。
3. 标题往返；resizable 往返。
4. contentPane 链：add 落到 contentPane、setContentPane 往返、null → ICSE；JMenuBar 往返。
5. root/layered/glass 窗格往返 + null 拒绝。
6. pack + setSize 往返 + setVisible(false) 保持隐藏；dispose 后全部窗口 !isDisplayable。
