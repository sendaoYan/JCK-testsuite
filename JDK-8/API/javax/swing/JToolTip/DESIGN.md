# javax.swing.JToolTip 测试设计

## 1. API 功能
工具提示（`extends JComponent`，implements `Accessible`）：承载 tipText 与宿主 component 两个
可空属性，由 ToolTipManager 通过 setToolTipText 触发创建；UI 委托渲染文本。

## 2. Java 用户如何使用
程序侧几乎不直接构造：`component.setToolTipText("x")` 后 ToolTipManager 按需 `new JToolTip()`
→ `setTipText` → `setComponent(component)`。直接使用仅限自定义提示位置场景。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JToolTip.java`；
`ToolTipManager.java`（管理器持有并复用 JToolTip 实例）。

## 4. 测试用例设计 + 易错细节
- 初始 tipText/component 均 null；**两个 setter 的 null 全部空安全**（置 null 清除，不抛）。
- setComponent 身份保持、可重绑到第二个宿主再清空。
- **属性分工易错点**：setTipText/getTipText 是 JToolTip 自身的数据；
  setToolTipText/getToolTipText 是 JComponent 级属性——setToolTipText("x") 不馈入 getTipText。
- UI 绑定（Metal 先行）：MetalToolTipUI / getUIClassID=="ToolTipUI" / updateUI 重绑无异常。
- getAccessibleContext 非空。
- headless 全程安全（无 Window 依赖，无需 @key）。

自动化实现位于同目录 `JToolTipTest.java`：反射 shape 保留 + 行为部分，
`PASS (N assertions)` 收尾。
