# javax.swing.JComponent 测试设计

## 1. API 功能
- 抽象基类（用匿名子类 `new JComponent(){}` 或 JPanel 实例测试），是多数 Swing 组件的父类。
- 基础属性：`setEnabled/isEnabled`、`setVisible/isVisible`、`setOpaque/isOpaque`、`setToolTipText/getToolTipText/getToolTipText(MouseEvent)`、`TOOL_TIP_TEXT_KEY`。
- 客户端属性：`putClientProperty/getClientProperty`。
- 尺寸/边框：`setPreferredSize/getPreferredSize`、`setMaximumSize/getMaximumSize`、`setMinimumSize/getMinimumSize`、`setBorder/getBorder`。
- 外观：`setFont/getFont`、`setForeground/getForeground`、`setBackground/getBackground`、`setAlignmentX/setAlignmentY`。
- 其他：`setAutoscrolls/getAutoscrolls`、`setInheritsPopupMenu/getInheritsPopupMenu`、`setComponentPopupMenu/getComponentPopupMenu`。

## 2. Java 用户如何使用
典型组合：自定义组件继承 JComponent 后，用 `setPreferredSize`、`setBorder`、`setToolTipText`、`putClientProperty` 配置外观与元数据；容器通过 `getPreferredSize`/`isOpaque`/`getFont` 决定布局与绘制。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JComponent/bug4706883.java` 与 `JToolTip/bug4225314.java`：验证 JComponent 的 client property/tooltip/布局行为。
- JDK 源码 `javax.swing.JToolBar.setRollover`：内部调用 `putClientProperty("JToolBar.isRollover", ...)`，是 client property 的典型自用。
- 大量 Swing 组件（JPanel/JLabel 等）继承 JComponent 并重写 `getPreferredSize`/`paintComponent`。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：TOOL_TIP_TEXT_KEY 常量；默认 enabled/opaque/tooltip/clientProperty/border/font/foreground/background 状态；enabled、tooltip（含 MouseEvent 变体）、clientProperty（含对象键与 null 移除）往返；preferred/maximum/minimum 尺寸往返；border、visible、font、foreground、background、opaque、alignment、autoscrolls、inheritsPopupMenu、componentPopupMenu 往返。见 `JComponentTest.java` 的 `testBlackBox()`。
