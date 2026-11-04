# java.awt.Frame 黑盒行为测试设计

## ① API 功能分组
- 构造：`Frame()`、`Frame(String)`、`Frame(GraphicsConfiguration)`、`Frame(String, GraphicsConfiguration)`。
- 标题：`setTitle/getTitle`。
- 外观：`setUndecorated/isUndecorated`、`setResizable/isResizable`。
- 菜单/图标：`setMenuBar/getMenuBar`、`setIconImage/getIconImage`。
- 状态：`getState/setState`、`setMaximizedBounds/getMaximizedBounds`、`setCursor/getCursorType`。
- 生命周期/查询：`dispose/getFrames/isDisplayable/isShowing`。

## ② Java 用户用法
```java
Frame f = new Frame("title");
f.setResizable(false); f.setUndecorated(true);
f.setMenuBar(new MenuBar());
f.setSize(200, 150); f.setVisible(true);
f.dispose();
```
组合链路：构造 → 标题/外观/菜单/图标/几何 setter → setVisible → getFrames → dispose。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Frame.setMenuBar` 移除旧菜单栏并触发 `removeNotify`（`Frame.java`）。
- `Frame.setUndecorated` 在显示前/后不同处理（`Frame.java`）。
- `Frame.getFrames` 通过 `Window.getWindows` 过滤 Frame 实例（`Frame.java`）。

## ④ 测试用例设计 + 自动化实现
- 标题/可缩放/无装饰/菜单栏/图标/最大化边界/光标类型 setter-getter 往返 → `testBlackBox`。
- 默认状态 NORMAL + 光标类型 DEFAULT_CURSOR → `testBlackBox`。
- setVisible→isDisplayable/isShowing→getFrames→dispose→!isDisplayable → `testBlackBox`。
