# javax.swing.JPanel 黑盒行为测试设计

## 1. API 功能
JPanel 是 Swing 最常用的轻量级容器，直接继承 JComponent，实现 Accessible。

- 构造器：`JPanel()`（默认 FlowLayout + 双缓冲）、`JPanel(LayoutManager)`、`JPanel(boolean isDoubleBuffered)`、`JPanel(LayoutManager, boolean)`。
- 布局：构造时传入 LayoutManager，或后续 `setLayout`；默认 FlowLayout。
- 缓冲/不透明：`isDoubleBuffered()`、`isOpaque()`（默认 true）。
- UI 委托：`getUI()`（返回 PanelUI）、`getUIClassID()`（返回 "PanelUI"）、`updateUI()`、`setUI(PanelUI)`。
- 组件属性（继承 JComponent）：`setBorder/getBorder`、`setBackground/getBackground`、`setForeground/getForeground`、`setToolTipText/getToolTipText`、`setEnabled/isEnabled`、`putClientProperty/getClientProperty`、`setPreferredSize/getPreferredSize`、`setMinimumSize/getMinimumSize`、`setMaximumSize/getMaximumSize`、`add/remove/removeAll`、`getAccessibleContext`。

## 2. Java 用户如何使用
- `JPanel p = new JPanel(new BorderLayout()); p.add(child, BorderLayout.CENTER);` —— 指定布局并挂子组件。
- `p.putClientProperty("key", value); Object v = p.getClientProperty("key");` —— 附着任意元数据。
- `p.setBorder(BorderFactory.createLineBorder(Color.BLACK));` —— 画边框。
- 布局确定性验证：`child.setPreferredSize(...); p.setSize(w,h); p.doLayout(); assert child.getBounds();`。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）大量使用 JPanel 作为容器，例如：
- `test/jdk/java/awt/Cursor/JPanelCursorTest/JPanelCursorTest.java` 用 JPanel 测光标。
- `test/jdk/java/awt/dnd/MouseExitGestureTriggerTest.java` 用 JPanel 作为拖放目标面板。
- `test/jdk/java/awt/Robot/HiDPIScreenCapture/*` 用 JPanel 承载绘制内容。
真实项目（Swing IDE、报表预览等）的通用模式是 `new JPanel(new BorderLayout())` + `setBorder` + `putClientProperty` 记录业务状态。以上来源均为 `~/git/jdk25u-dev/test/jdk`，未编造。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JPanelTest.testBlackBox()`）：
1. 默认构造：FlowLayout / 双缓冲 / 不透明 / getUI 非 null 且 PanelUI / getUIClassID。
2. 四种构造器各自的布局与双缓冲语义。
3. clientProperty 往返（含 null 与缺失键）。
4. Border 往返（setBorder/getBorder 引用相等 + setBorder(null)）。
5. 颜色 / tooltip / enabled 往返。
6. preferred/min/max size 往返。
7. 子组件 add/removeAll + BorderLayout 常数驱动布局断言（CENTER 填满）。
8. getAccessibleContext 非 null。
