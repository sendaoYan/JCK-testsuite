# java.awt.Window 黑盒行为测试设计

## ① API 功能分组
- 构造：`Window(Frame)`、`Window(Window)`、`Window(Window, GraphicsConfiguration)`（3 个 public 构造器；null owner 允许）。
- 归属：`getOwner/getOwnedWindows/getWindows/getOwnerlessWindows`。
- 模态排除：`setModalExclusionType/getModalExclusionType`；类型：`setType/getType`。
- 生命周期：`setVisible/show/hide/dispose/toFront/toBack/isDisplayable/isShowing`；窗口事件 WINDOW_OPENED(200)/WINDOW_CLOSED(202)。
- 置顶/焦点：`setAlwaysOnTop/isAlwaysOnTop/isAlwaysOnTopSupported`、`setFocusableWindowState/getFocusableWindowState/isFocusableWindow`。
- 几何：`setSize/setLocation/setBounds/setLocationRelativeTo/setLocationByPlatform/isLocationByPlatform/pack/getToolkit`。
- 图标：`setIconImage/setIconImages/getIconImages`（Window 层声明；getIconImage 单数在 Frame）。
- 监听器族：window/windowFocus/windowState 三族注册查询。

## ② Java 用户用法
```java
Window w = new Window(frame);
w.setSize(200, 150); w.setLocation(40, 50);
w.setType(Window.Type.UTILITY);
w.setVisible(true);
w.dispose();
```
组合链路：构造 → 设几何/类型 → setVisible → 事件日志（EventQueue.invokeAndWait 排空）→ 查询 owner/windows → dispose。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Window.getOwnedWindows` 遍历 `ownedWindowList`（`Window.java`）。
- `Window.setVisible` 首次显示前 addNotify、发 WINDOW_OPENED 一次；`dispose` 发 WINDOW_CLOSED（`Window.java`）。
- `Window.setLocationRelativeTo(null)` 按默认屏幕 bounds 居中（`Window.java`）。
- `setLocation/setBounds` 清除 locationByPlatform 标志（文档化行为）。

## ④ 测试用例设计 + 自动化实现
- owner/ownedWindows/getWindows/getOwnerlessWindows 归属查询（frame ownerless、owned 不在）→ `testConstructorsAndOwnership`/`testBlackBox`。
- 模态排除/类型/几何/背景 setter-getter 往返 → `testBlackBox`。
- setVisible→isDisplayable/isShowing→dispose→!isDisplayable 生命周期 → `testBlackBox`。
- 生命周期事件铁律：首次 show 恰一条 WINDOW_OPENED（id=200、source/window 精确）；hide 不发 CLOSED；重 show 不再发 OPENED；dispose 发 CLOSED(202)；二次 dispose no-op；dispose 后重 show 重建 peer 且不再发 OPENED；全部经 `EventQueue.invokeAndWait` 排空 → `testLifecycleEvents`。
- 几何契约：默认 invisible；`setSize(0,0)` 与负尺寸在 peer 创建前原样存储；setBounds 往返；`setLocationRelativeTo(null)` 按运行时默认 gc bounds 精确居中公式；`setLocationByPlatform` 被 setLocation/setBounds 文档化清除；`getToolkit==getDefaultToolkit`；pack 后 insets 非空 → `testGeometryContracts`。
- null owner 三构造形态 getOwner()==null；`(owner,null gc)` 保留 owner → `testConstructorsAndOwnership`。
- 窗口监听器族：null 注册 no-op；重复 add 计数 3/单 remove 删一个/未注册 remove no-op；`getListeners` 三族一致 → `testWindowListenerFamilies`。

## ⑤ 反例与精确异常
- Window 家族本轮覆盖的均为行为断言（no-op/计数），无必须锁定的异常路径；`toFront/toBack` 仅断言不抛。

自动化实现位于同目录 `WindowTest.java`：`check` 计数断言，`main` 打印 `PASS (N assertions)`；@key headful（DISPLAY=:77，无 WM 下 insets=0 保证确定性），通过 110 断言。
