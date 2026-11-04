# java.awt.Window 黑盒行为测试设计

## ① API 功能分组
- 构造：`Window(Frame)`、`Window(Window)`、`Window(Window, GraphicsConfiguration)`。
- 归属：`getOwner/getOwnedWindows/getWindows`。
- 模态排除：`setModalExclusionType/getModalExclusionType`。
- 类型：`setType/getType`。
- 生命周期：`setVisible/dispose/isDisplayable/isShowing`。
- 置顶/焦点窗口状态：`setAlwaysOnTop/isAlwaysOnTop/isAlwaysOnTopSupported`、`setFocusableWindowState/getFocusableWindowState/isFocusableWindow`。
- 几何/外观：`setSize/setLocation/setBackground`。

## ② Java 用户用法
```java
Window w = new Window(frame);
w.setSize(200, 150); w.setLocation(40, 50);
w.setType(Window.Type.UTILITY);
w.setVisible(true);
w.dispose();
```
组合链路：构造 → 设几何/类型 → setVisible → 查询 owner/windows → dispose。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Window.getOwnedWindows` 遍历 `ownedWindowList`（`Window.java`）。
- `Window.setVisible` 首次显示前 addNotify 并加入全局窗口列表（`Window.java`）。
- `Window.setType` 校验类型并同步对等体（`Window.java`）。

## ④ 测试用例设计 + 自动化实现
- owner/ownedWindows/getWindows 归属查询 → `testBlackBox`。
- 模态排除/类型/几何/背景 setter-getter 往返 → `testBlackBox`。
- setVisible→isDisplayable/isShowing→dispose→!isDisplayable 生命周期 → `testBlackBox`。
- alwaysOnTop/焦点窗口状态标志位 → `testBlackBox`。
