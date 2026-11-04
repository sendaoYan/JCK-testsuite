# javax.swing.SwingUtilities 测试设计

## 1. API 功能
Swing 静态工具箱：矩形运算（computeIntersection/computeUnion/isRectangleContainingRectangle）、
坐标转换（convertPoint/convertRectangle）、组件树查询（getAncestorOfClass/Named、
getDeepestComponentAt、getWindowAncestor、getRoot、getLocalBounds）、
layoutCompoundLabel 图标文本几何、EDT 工具（isEventDispatchThread/invokeAndWait）。

## 2. Java 用户如何使用
`SwingUtilities.convertPoint(child, p, target)` 跨层坐标；`SwingUtilities.invokeAndWait(...)` 
从非 EDT 线程安全驱动 UI；LabelUI 内部 layoutCompoundLabel 决定 icon/text 摆放。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/SwingUtilities.java`；
`test/jdk/javax/swing/`（SwingUtilitiesFactory 等）。

## 4. 测试用例设计 + 易错细节
- **computeIntersection/computeUnion 原地修改并返回同一实例**（got==r 引用断言）；
  不相交交集塌成原点 0x0（不是 null 也不是原矩形）。
- isRectangleContainingRectangle 含边界（相等矩形 true；重叠但超出 false）。
- convertPoint/convertRectangle 父子方向对称：子(5,5)→父(15,25)，父(15,25)→子(5,5)；
  int 重载与 Point 重载一致；转换用 bounds 链，组件 setBounds 后无需显示即可用
  （布局管理器需 setLayout(null) 防 doLayout 覆写）。
- getAncestorOfClass 返回**最近**匹配祖先；getDeepestComponentAt 越界 (999,999) → null；
  独立组件 getWindowAncestor/getRoot 均 null。
- getLocalBounds 反映当前尺寸（setSize(20,10) → (0,0,20,10)），未显示时 0x0。
- **layoutCompoundLabel 用空文本做 icon-only 几何锁定**（TOP/LEFT → (0,0,10,10)、
  BOTTOM/RIGHT → (90,10,10,10)、CENTER → (45,5,10,10)）——空文本与字体无关，值全平台稳定；
  带文本时 textR 宽度随字体变化，勿锁字面值。
- isLeft/Middle/RightMouseButton 用 InputEvent.BUTTON1/2/3_MASK 构造 MouseEvent（headless 可造），
  交叉组合全 false。
- invokeAndWait：体内在 EDT；**体内抛出的 RuntimeException 包装为 InvocationTargetException
  （cause 保留）**；computeStringWidth 恒等于 FontMetrics.stringWidth。

自动化实现位于同目录 `*Test.java`。
