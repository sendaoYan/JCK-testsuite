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

## 5. 组件层深化批（headless，Metal LaF + invokeAndWait）
`testContract()` 补充：标准 setter 的 PropertyChangeEvent 事件名逐一锁定
（opaque/border/enabled/font/background/foreground 小写、**ToolTipText 大写驼峰**）；
`registerKeyboardAction(null,"cmdA",ks,WHEN_FOCUSED)` 绑定落 InputMap 且值为
`ActionStandin`、不写 ActionMap、unregister 后 InputMap/registeredKeyStrokes 双清零；
setRequestFocusEnabled、getDefaultLocale 静态往返、popup 菜单继承/组件往返；
setEnabled 不级联子件 + 显式 disabled 子件跨父周期 sticky；未显示时
`getVisibleRect()==(0,0,0,0)`、`getTopLevelAncestor()==null`、repaint/revalidate/
scrollRectToVisible 无 peer 不抛；ancestor/vetoable 监听 add-remove 对称计数；
双缓冲/优化绘制开关；`PanelUI`/`BasicPanelUI` 身份；父容器后设 setFont 不替换子
JLabel 的 LaF 字体；AccessibleContext 无 peer 可用。

**JDK-27 特有版本差异**：`JComponent.getUI()` 在本版本为 public（返回
`javax.swing.plaf.ComponentUI`，用 `getMethod("getUI")` 反射锁定），JDK-8 上该方法
非 public——这是两侧唯一的行为面差异，其余契约双侧一致。
