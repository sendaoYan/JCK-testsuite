# java.awt.Window 黑盒行为测试设计（JDK-8）

## 1. API 功能
顶层窗口（extends Container）：3 构造器（Window(Frame)/Window(Window)/Window(Window,gc)，null owner 允许）、
归属 getOwner/getOwnedWindows/getWindows/getOwnerlessWindows、模态排除、setType/getType、
生命周期 setVisible/dispose/toFront/toBack、窗口事件 WINDOW_OPENED(200)/WINDOW_CLOSED(202)、
alwaysOnTop/focusableWindowState、几何 setSize/setLocation/setBounds/setLocationRelativeTo/setLocationByPlatform/pack。

## 2. Java 用户如何使用
`Window w = new Window(frame); w.setSize(120,90); w.setVisible(true);`
事件经 `EventQueue.invokeAndWait(() -> {})` 排空后断言恰一条 WINDOW_OPENED；dispose 得 WINDOW_CLOSED。

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Window.java`
（setVisible 首显 addNotify+OPENED 一次；dispose 发 CLOSED；setLocationRelativeTo(null) 按默认屏幕居中；
setLocation/setBounds 清 locationByPlatform）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集，含 applyResourceBuffer/postEvent 等旧 API）→ `testStructure`。
- 显示路径 owner/owned/windows/模态排除/类型/alwaysOnTop/焦点窗口状态/背景往返 + dispose → `testBlackBox`。
- 构造与归属图：Window(Window) 链、三 null-owner 形态 getOwner()==null、ownerless 判定 → `testConstructorsAndOwnership`。
- 生命周期事件铁律：首 show 恰一条 OPENED(200)；hide 无 CLOSED；重 show 无第二条 OPENED；
  **dispose 发 CLOSED(202)、二次 dispose no-op、dispose 后重 show 重建 peer 且不再 OPENED（易错）**
  → `testLifecycleEvents`。
- 几何契约：默认 invisible、setSize(0,0)/负尺寸 peer 前原样存储、setBounds 往返、
  setLocationRelativeTo(null) 按运行时 gc bounds 精确居中公式、locationByPlatform 被 setLocation/setBounds 清除、
  getToolkit==默认、pack → `testGeometryContracts`。
- 三族监听器注册/重复/移除契约 + getListeners 一致 → `testWindowListenerFamilies`。
- JDK-8 侧 148 断言通过（DISPLAY=:77 无 WM，insets=0，@key headful）。
