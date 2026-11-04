# javax.swing.JToolBar 测试设计

## 1. API 功能
工具栏（`extends JComponent`，implements `SwingConstants/Accessible`）：方向、floatable 拖拽、
rollover、margin、add(Action)/add(Component)/addSeparator 装配、
getComponentIndex/getComponentAtIndex 索引访问。

## 2. Java 用户如何使用
`new JToolBar()` → `add(action)` 批量装按钮（文本自动取 Action 名）→ `addSeparator()` 分组 →
`setFloatable(true)` 允许拖出；`setRollover(true)` 平面化外观。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JToolBar.java`
（add(Action) 构造 JButton 并同步 text/enabled）；`test/jdk/javax/swing/JToolBar/`。

## 4. 测试用例设计 + 易错细节
- 默认（Metal）：HORIZONTAL / floatable true / **rollover false** / **margin Insets(0,0,0,0)** /
  0 子组件 / MetalToolBarUI / getUIClassID=="ToolBarUI"。
- 构造器 4 变体（无参/方向/名字/名字+方向）。
- **add(Action) 契约**：返回 JButton、text 同步自 Action 名、getAction() 身份一致、
  **setEnabled 双向传播**、doClick 经 Action 派发。
- 索引访问边沿：getComponentIndex(外来)==−1；getComponentAtIndex(count/99/−1)==null（不抛）；
  remove 后余项重排。
- **addSeparator 产出的是 JToolBar$Separator 而非 JSeparator**；两个重载各 +1 子组件。
- setMargin/setFloatable/setRollover 往返；setOrientation(99)/(-5) IAE 且拒绝后方向保持。

自动化实现位于同目录 `JToolBarTest.java`；EDT 纪律 + Metal LaF 先行。
