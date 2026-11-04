# javax.swing.ViewportLayout 测试设计

## 1. API 功能
JViewport 的布局管理器（LayoutManager + Serializable）：单子件"视口-视图"布局——
preferredLayoutSize 委托 view 的 preferred（Scrollable 视图走
getPreferredScrollableViewportSize）；minimumLayoutSize 恒 4x4 常量；
layoutContainer 把 view 拉伸到 viewport 尺寸；add/removeLayoutComponent 是容忍性 no-op。

## 2. Java 用户如何使用
JViewport 默认安装（viewport.getLayout() 即其实例），无需手动创建；自定义视口
行为时才 `viewport.setLayout(new ViewportLayout())`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/ViewportLayout.java`；
JViewport 构造器安装路径；`test/jdk/javax/swing/` 间接覆盖。

## 4. 测试用例设计 + 易错细节
- 默认安装：任意 new JViewport().getLayout() instanceof ViewportLayout。
- 尺寸委托：view（非 Scrollable，JPanel）固定 preferred 37x23 →
  preferredLayoutSize==(37,23)；无 view 时 0x0。
- **minimumLayoutSize 是常量 4x4**：不读 view 的 minimum（即使
  view.setMinimumSize(5,6) 也不影响）——"minimum 不委托"是与 preferred 的对照点。
- 拉伸语义：viewport setSize(50,40) 后 layoutContainer → view bounds==(0,0,50,40)
  （view 被拉满视口，不保持自身 preferred）。
- addLayoutComponent(String,comp)/removeLayoutComponent 任意参数 no-throw。
- **layoutContainer(非 JViewport 容器)→ClassCastException**（源码里直接强转 parent）。
- 环境：headless 下 Metal LaF 先装；组件操作 EDT。

自动化实现位于同目录 `ViewportLayoutTest.java`：`check(boolean,msg)` 计数断言，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
