# javax.swing.JPanel 测试设计

## 1. API 功能
通用轻量容器（JComponent + Accessible）：默认 FlowLayout + 双缓冲 + 不透明；
四构造器 `()`/`(boolean)`/`(LayoutManager)`/`(LayoutManager, boolean)`；
UI 委托 PanelUI（Metal 下 BasicPanelUI）。

## 2. Java 用户如何使用
最常用的组装底板：`JPanel p = new JPanel(new BorderLayout()); p.add(toolBar, NORTH);`
表单/按钮排/卡片页；`(LayoutManager, boolean)` 可关双缓冲做截屏或性能实验。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JPanel.java`；
仓库与 JDK 自带测试里作为容器底板大量出现（`test/jdk/javax/swing/**`）。

## 4. 测试用例设计 + 易错细节
- 默认契约：FlowLayout/双缓冲 true/不透明 true/isOptimizedDrawingEnabled true/
  getUIClassID=="PanelUI"/Metal 下 UI SimpleName=="BasicPanelUI"。
- 构造器矩阵：JPanel(false) 关双缓冲但仍是 FlowLayout；JPanel(layout) 布局生效、
  双缓冲仍 true；JPanel(layout,false)/JPanel(layout,true) 显式双缓冲。
- setLayout 可任意换（GridLayout↔FlowLayout 往返），updateUI 后 UI 仍是 PanelUI。
- 属性往返：clientProperty（put null 移除）、Border（含 null）、背景/前景/tooltip/
  enabled/preferred-min-max size。
- 子件簿记：add/remove(int)/removeAll 后 getComponentCount 与
  getComponent(int)（恒等+类型）一致。
- 固定几何：BorderLayout 面板 setSize(200,150)+doLayout 后 CENTER 子件 bounds
  精确 ==(0,0,200,150)（子件固定 preferred，不依赖字体）。
- 环境：headless 下 Metal LaF 先装；EDT 操作。

自动化实现位于同目录 `JPanelTest.java`：`check(boolean,msg)` 计数断言，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
