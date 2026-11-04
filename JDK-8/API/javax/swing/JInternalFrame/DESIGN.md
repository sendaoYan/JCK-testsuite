# javax.swing.JInternalFrame 测试设计（JDK-8 侧）

## 1. API 功能
MDI 子窗口：构造器参数序 (title, resizable, closable, maximizable, iconifiable)；
能力位 setter + bound property 事件；RootPaneContainer 四窗格契约；
VetoableChangeListener 对 vetoable 属性（closed/maximum/icon/selected）的否决语义。

## 2. Java 用户如何使用
`new JInternalFrame("Doc", true, true, true, true)` → `desktop.add(f)` → `f.setVisible(true)`；
关闭 `setClosed(true)`（closable=false 时仍生效）； veto 监听器抛 `PropertyVetoException` 阻止关闭。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`jdk8u/src/share/classes/javax/swing/JInternalFrame.java`；
JDK 自带测试 `test/javax/swing/JInternalFrame/bug4305284.java` 等；本仓库 probe（/tmp/probe-f）。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF + EDT）
- 构造器矩阵：无参 title==""（空串非 null）；2/3/4/5 参逐位验证参数序。
- **属性事件名：setIconifiable 触发的是 "iconable"（非 "iconifiable"）**；title/closable/resizable/maximizable 同名。
- **setClosed(true) 在 closable==false 时仍生效**（no-throw 且 isClosed==true）。
- veto：VetoableChangeListener 否决 "closed" → setClosed 抛 PVE、isClosed 保持 false。
- **JDK-8 版本差异：setMaximum(true) 在 headless 下从 UI 路径抛 NPE，但 maximized 标志已置位
  （isMaximum()==true），随后 setMaximum(false) 可恢复；JDK-27 侧同一调用不抛、直接成功**。
- 未显示时 setSelected(true) no-throw 且 isSelected 仍 false；setIcon(true/false) headless 无 veto 往返。
- RootPaneContainer null 契约：setContentPane(null)/setLayeredPane(null)→IllegalComponentStateException，
  setGlassPane(null)→NPE；contentPane 默认布局 BorderLayout；setJMenuBar(null) 合法。
- setDefaultCloseOperation 默认 2 (DISPOSE_ON_CLOSE)；setLayer 往返；getDesktopPane 未挂载 null；
  getWarningString null；pack/toFront/toBack/moveToFront/moveToBack headless no-throw；getUI Metal。

自动化实现位于同目录 `JInternalFrameTest.java`：`check` 计数断言、`checkThrows` 校验精确异常类型，
结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（无 @key，headless 可跑）。
