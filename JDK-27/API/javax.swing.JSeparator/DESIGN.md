# javax.swing.JSeparator 测试设计

## 1. API 功能
- 构造器：`JSeparator()`（默认 HORIZONTAL）、`JSeparator(int orientation)`。
- 方向：`setOrientation/getOrientation`（仅 HORIZONTAL/VERTICAL，非法抛 `IllegalArgumentException`）。
- UI：`getUI/setUI/updateUI/getUIClassID`（返回 "SeparatorUI"）。
- 继承自 JComponent 的大小/属性：`setMinimumSize/getMinimumSize`、`setMaximumSize/getMaximumSize`、`setPreferredSize/getPreferredSize`，以及 enabled/visible/tooltip/color/opaque/name 等。

## 2. Java 用户如何使用
典型组合：`new JSeparator(SwingConstants.VERTICAL)` 放进 `JToolBar` 或布局作为分隔条；`setOrientation(HORIZONTAL)` 切换方向；必要时 `setPreferredSize(new Dimension(...))` 控制分隔条粗细。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JToolBar/ToolBarSeparatorSizeTest.java`：向 JToolBar 添加 JSeparator 并验证尺寸。
- JDK 源码 `javax.swing.JToolBar.addSeparator`：内部 `new JToolBar.Separator(size)`（继承 JSeparator）并据工具栏方向设置 `setOrientation`。
- 常见 UI 用 JSeparator 在菜单/工具条里做视觉分隔。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：默认方向 HORIZONTAL、不可聚焦、UI 类名、UI 非 null；VERTICAL 构造器；方向往返与非法值 `IllegalArgumentException`；minimum/maximum/preferred 尺寸往返；继承属性 enabled/visible/tooltip/background/foreground/opaque/name 的往返。见 `JSeparatorTest.java` 的 `testBlackBox()`。
