# javax.swing.JRootPane 测试设计

## 1. API 功能
顶层容器根结构（JComponent + Accessible）：管理 contentPane/layeredPane/glassPane
三层（layeredPane 内 FRAME_CONTENT_LAYER=-30000 承载 contentPane+menuBar）、
JMenuBar/defaultButton 挂点、windowDecorationStyle（NONE/FRAME/6 种对话框样式，
1..8）、isValidateRoot；getUIClassID=="RootPaneUI"。

## 2. Java 用户如何使用
`JDialog frame` 类内部自动建 JRootPane；程序侧
`frame.getRootPane().setDefaultButton(okButton)`（回车触发）、
`rootPane.setWindowDecorationStyle(JRootPane.FRAME)` 启用 Swing 自绘标题栏。
直接 `new JRootPane()` 用于自定义顶层容器/单元测试。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JRootPane.java`；
JFrame/JDialog/JWindow/JInternalFrame 的 rootPaneCheckingException 用法；
`test/jdk/javax/swing/JRootPane/`。

## 4. 测试用例设计 + 易错细节
- 常量矩阵：NONE=0/FRAME=1/PLAIN=2/INFO=3/ERROR=4/COLOR_CHOOSER=5/FILE_CHOOSER=6/
  QUESTION=7/WARNING=8（5、6、7 的非连续编号易错）。
- 默认接线：contentPane 布局是 BorderLayout（JRootPane$1 匿名子类——只断
  instanceof）；contentPane.parent==layeredPane、layeredPane.parent==root；
  glassPane 是 JComponent、不透明 false、不可见 false；
  默认 menuBar/defaultButton null；isValidateRoot true；Metal→MetalRootPaneUI。
- 换件矩阵：setContentPane(new) 后新件挂 layeredPane、旧件 parent==null、
  FRAME_CONTENT_LAYER 恰 1 件；setLayeredPane(new) 后 contentPane 引用保留；
  setGlassPane(gp) 后 gp.parent==root。
- **null 反例三种异常各不相同**：setContentPane(null)→IllegalComponentStateException、
  setLayeredPane(null)→IllegalComponentStateException、setGlassPane(null)→NPE；
  setJMenuBar(null) 合法清空；deprecated getMenuBar/setMenuBar 与新 API 一致。
- defaultButton：设置即触发 propertyName=="defaultButton" 事件（old null/new 按钮）；
  无窗口也生效；setDefaultButton(null) 合法。
- **setWindowDecorationStyle 语义（重点）**：合法值先写入字段再应用——headless 下
  FRAME 抛 HeadlessException、对话框样式抛 NPE，**但 getWindowDecorationStyle()
  已是新值**（write-then-throw）；非法值（99）在赋值前就 IAE，字段保持原值。
- 版本/环境差异：本文件含 JFrame 实窗块（@key headful，DISPLAY 跑）；headless 反例用 isHeadless() 门控；
  headless 反例块用 GraphicsEnvironment.isHeadless() 门控，两种环境同一文件全绿。

自动化实现位于同目录 `JRootPaneTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印
`PASS (N assertions)`；jtreg `@run main` 直接驱动。
