# javax.swing.JComponent 测试设计

## 1. API 功能
Swing 组件抽象基类（java.awt.Container 之上）：客户端属性、tooltip、边框/字体/颜色、
键盘绑定（InputMap/ActionMap）、请求焦点开关、默认 Locale（静态）、弹出菜单继承、
重绘/重校验钩子、双缓冲与优化绘制标志、ancestor/vetoable 监听器、UI 委托（getUIClassID/getUI）。

## 2. Java 用户如何使用
自定义组件继承 JComponent 后用 `putClientProperty` 挂元数据、`registerKeyboardAction`
注册快捷键、`setBorder/setFont` 配置外观；容器侧靠 `getPreferredSize/isOpaque/getFont` 布局绘制。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComponent.java`；
JDK 自带 `test/jdk/javax/swing/JComponent/`；JToolBar.setRollover 内部用
`putClientProperty("JToolBar.isRollover", ...)` 是 client property 典型自用。

## 4. 测试用例设计 + 易错细节
- headless 安全：设 MetalLookAndFeel 后全部断言可在无 DISPLAY 下跑（`env -u DISPLAY`），
  EDT 纪律用 `SwingUtilities.invokeAndWait` 包住整个测试体。
- client property：`putClientProperty(k, null)` 是**删除键**（不是存 null）；任意对象可作键。
- tooltip：`setToolTipText(null)` 清空；属性事件名是 `"ToolTipText"`（大写驼峰，与其他 setter 的
  小写名不同——易错点，逐一断言：opaque/border/enabled/font/background/foreground）。
- `registerKeyboardAction(null,"cmdA",ks,WHEN_FOCUSED)`：绑定落进 InputMap，值是
  `JComponent$ActionStandin`（非命令字符串本身）；不往 ActionMap 写 "cmdA" 条目；
  `getRegisteredKeyStrokes()` 与 InputMap 同步，`unregisterKeyboardAction` 后双双清零。
- `setEnabled` 不级联子件；子件显式 disabled 后父件 enable 周期不影响（sticky）。
- 几何：未显示时 `getVisibleRect()==(0,0,0,0)`、`getTopLevelAncestor()==null`；
  `repaint/revalidate/scrollRectToVisible` 无 peer 下不抛异常。
- 字体：父容器后设的 setFont **不替换**子 JLabel 的 LaF 字体（LaF 用 UIResource 标记）。
- UI 身份：`getUIClassID()=="PanelUI"`（JPanel），Metal 下 getUI() 是
  `javax.swing.plaf.basic.BasicPanelUI`（Metal 不覆盖 PanelUI）。
- 监听器计数：ancestor/vetoable add-remove 对称。
- JDK-8 特有：`getUI()` 在 JComponent 上**不是** public API（JDK-27 才公有化），
  本侧不直接调用 `p.getUI()`，改经 `JPanel.getUI()`（JPanel 覆盖为 public）。

自动化实现位于同目录 `*Test.java`：`check(boolean,msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动，无 @key（headless）。
