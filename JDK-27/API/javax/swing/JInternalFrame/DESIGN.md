# javax.swing.JInternalFrame 测试设计

## 1. API 功能
- 构造器：`JInternalFrame()`（默认空标题、全部能力 false）、`JInternalFrame(String[, boolean resizable[, boolean closable[, boolean maximizable[, boolean iconifiable]]]])`。
- 属性：`setTitle/getTitle`、`setClosable/isClosable`、`setIconifiable/isIconifiable`、`setMaximizable/isMaximizable`、`setResizable/isResizable`、`isClosed/isMaximum/isIcon/isSelected`。
- 选择/关闭/图标/最大化：`setSelected/isSelected`（需在可见桌面上才能选中）、`setClosed/isClosed`、`setIcon/isIcon`、`setMaximum/isMaximum`。
- RootPaneContainer：`getRootPane/getContentPane/getLayeredPane/getGlassPane`；菜单 `setMenuBar/getMenuBar`。
- 其他：`getDefaultCloseOperation/setDefaultCloseOperation`、`getLayer/setLayer`、`getDesktopPane`、`getNormalBounds/setNormalBounds`、`getFrameIcon/setFrameIcon`。

## 2. Java 用户如何使用
典型组合：`new JInternalFrame("Doc", true, true, true, true)` → `desktop.add(f)` → `f.setVisible(true)` → `f.setSelected(true)`；关闭用 `setClosed(true)`（抛 `PropertyVetoException`）；菜单 `setMenuBar(new JMenuBar())`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JInternalFrame/bug4305284.java`、`Test6505027.java`、`8145896/TestJInternalFrameMaximize.java`：验证标题、选择、最大化等状态。
- JDK 源码 `javax.swing.JDesktopPane.getAllFrames` 依赖 `JInternalFrame` 的 layer/z-order 管理内部框架。
- 常见 MDI 应用（代码编辑器等）用 JInternalFrame 承载子窗口。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：默认构造器与 5 参构造器能力位；title/closable/iconifiable/maximizable/resizable 往返；isClosed/isMaximum/isIcon/isSelected 默认 false；默认关闭操作 DISPOSE_ON_CLOSE 及切换；RootPaneContainer 各窗格非 null；菜单往返；layer 往返；desktopPane 初始 null 与加入后命中；normalBounds/frameIcon 往返；在可见 JDesktopPane 内 `setVisible(true)` 后 `setSelected` 往返。见 `JInternalFrameTest.java` 的 `testBlackBox()`。

## 5. 追加契约段（2026-11 深化）
构造器 2/3/4/5 参逐位参数序；属性事件名（**setIconifiable 触发 "iconable"**）；
RootPaneContainer null 异常（setContentPane/setLayeredPane null→IllegalComponentStateException、
setGlassPane null→NPE）；**setClosed(true) 在 closable==false 时仍生效**；veto 否决保持 isClosed false；
**setMaximum(true) 无桌面也能完成（JDK-27 行为；JDK-8 侧同调用从 UI 路径抛 NPE——版本差异，各侧各自锁定）**；
未显示 setSelected no-throw 且 isSelected 不变；setIcon headless 往返；getUI/updateUI/accessible。
