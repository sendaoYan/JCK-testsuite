# javax.swing.JToolBar 测试设计

## 1. API 功能
- 构造器：`JToolBar()`、`JToolBar(int orientation)`、`JToolBar(String name)`、`JToolBar(String name, int orientation)`。
- 布局/方向：`setOrientation(int)/getOrientation()`、`setLayout(LayoutManager)`（内部用 DefaultToolBarLayout）。
- 组件管理：`add(Component)`、`add(Action)`（返回 JButton）、`addSeparator()`、`addSeparator(Dimension)`、`getComponentIndex(Component)`、`getComponentAtIndex(int)`。
- 属性：`setFloatable/isFloatable`、`setRollover/isRollover`、`setBorderPainted/isBorderPainted`、`setMargin/getMargin`。

## 2. Java 用户如何使用
典型组合：`new JToolBar("main")` → `add(new JButton("Save"))` → `addSeparator()` → `getComponentIndex(btn)`；再把 `AbstractAction` 直接 `add(action)` 得到已配置按钮并 `doClick()` 触发动作。浮动/滚动外观用 `setFloatable(false)`、`setRollover(true)`，边距用 `setMargin(new Insets(...))`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JToolBar/ToolBarSeparatorSizeTest.java`：`new JToolBar()` 后向工具栏添加按钮与 `JSeparator`，验证分隔条不挤压按钮布局。
- JDK 源码 `javax.swing.plaf.basic.BasicToolBarUI`：UI 委托读取 `isFloatable`/`getOrientation` 决定拖拽与排布。
- 常见 Swing 应用（如 NetBeans 平台）用 `add(Action)` 批量装配工具栏按钮，并配合 `setRollover(true)` 获得悬停边框。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：默认方向/浮动/滚动/边框/空工具栏状态；各构造器；`add` 与 `getComponentIndex/getComponentAtIndex`（含越界与未知组件返回 -1/null）；`setFloatable/setRollover/setOrientation/setBorderPainted/setMargin` 往返；非法 orientation 抛 `IllegalArgumentException`；`addSeparator()` 与 `addSeparator(Dimension)` 产生 `JSeparator` 且横向工具栏分隔条为 VERTICAL；`add(Action)` 返回 JButton、文本取自 Action、`doClick()` 触发 actionPerformed。见 `JToolBarTest.java` 的 `testBlackBox()`。


## 5. 深化部分（`JToolBarTest.testDeepContracts`，Action 装配 + 分隔条 + 方向反例）
1. add(Action) 契约：返回 JButton、text 同步自 Action 名、getAction() 身份一致；action.setEnabled(false/true) 双向传播到按钮；doClick 经 Action 派发。
2. 索引访问边沿：getComponentIndex(外来按钮)==-1、getComponentAtIndex(count/99/-1)==null。
3. addSeparator()/addSeparator(Dimension) 各 +1 子组件，分隔条类名为 JToolBar$Separator（非 JSeparator）。
4. setMargin 往返；setOrientation(99)/(-5) 均 IAE 且拒绝后方向保持。
5. UI 绑定：MetalToolBarUI / getUIClassID=="ToolBarUI"。
