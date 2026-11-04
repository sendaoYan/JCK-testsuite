# java.awt.Frame 黑盒行为测试设计

## ① API 功能分组
- 构造：`Frame()`、`Frame(String)`、`Frame(GraphicsConfiguration)`、`Frame(String, GraphicsConfiguration)`（4 个 public 构造器；null gc 允许）。
- 标题：`setTitle/getTitle`（null 归一化为 ""，PCE `title|old|new`）。
- 外观：`setUndecorated/isUndecorated`、`setResizable/isResizable`。
- 菜单/图标：`setMenuBar/getMenuBar`、`setIconImage/getIconImage`（Frame 声明单数形态）、`setIconImages/getIconImages`（Window 层）。
- 状态机：常量 NORMAL=0/ICONIFIED=1/MAXIMIZED_HORIZ=2/MAXIMIZED_VERT=4/MAXIMIZED_BOTH=6；`getState/setState`、`getExtendedState/setExtendedState`、`setMaximizedBounds/getMaximizedBounds`。
- 光标：`setCursor(int)/getCursorType`（弃用）。
- 生命周期/查询：`dispose/getFrames/isDisplayable/isShowing/pack`。

## ② Java 用户用法
```java
Frame f = new Frame("title");
f.setResizable(false); f.setUndecorated(true);
f.setMenuBar(new MenuBar());
f.setSize(200, 150); f.setVisible(true);
f.dispose();
```
组合链路：构造 → 标题/外观/菜单/图标/几何 setter → setVisible → getFrames → dispose；状态机按 `Toolkit.isFrameStateSupported` 条件断言。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Frame.setMenuBar` 移除旧菜单栏并触发 `removeNotify`（`Frame.java`）。
- `Frame.setUndecorated` 在 peer 存在时抛 IllegalComponentStateException（`Frame.java`）。
- `Frame.getFrames` 通过 `Window.getWindows` 过滤 Frame 实例（`Frame.java`）。
- `Frame.setExtendedState` 对不支持状态直接 return 不存储（`Frame.java`）。

## ④ 测试用例设计 + 自动化实现
- 状态常量精确值 0/1/2/4/6 → `testBlackBox`。
- 4 构造器（默认 title ""、null gc OK、默认布局 BorderLayout）→ `testBlackBox`。
- 标题/可缩放/无装饰/菜单栏/图标/最大化边界/光标类型 setter-getter 往返；`setTitle(null)`→"" → `testBlackBox`。
- setVisible→isDisplayable/isShowing→getFrames→dispose→!isDisplayable → `testBlackBox`。
- 标题 PCE 精确载荷：`title||A`、`title|A|B`、`title|B|`（setTitle(null) 发空新值）、同值短路 → `testTitlePropertyChange`。
- 图标列表语义：默认空、setIconImages 拷贝源列表、getIconImages 返回拷贝、getIconImage==首个、setIconImage 单元素、空列表清空 → `testIconListSemantics`。
- 状态机：ICONIFIED 往返条件于 `isFrameStateSupported(ICONIFIED)`；`setState(88)` 掩码后保持 NORMAL；setExtendedState(不支持状态) 不存储（else 分支断言 NORMAL）；组合态条件往返 → `testStateContracts`。
- pack 后非负尺寸 + insets 非空 → `testPack`。

## ⑤ 反例与精确异常
- displayable 状态下 `setUndecorated(true)` → IllegalComponentStateException。
- `setExtendedState`(不支持位组合) → 静默忽略（源码 `if (!isFrameStateSupported(state)) return;`，测试锁"不存储"行为）。

版本差异：与 JDK-8 侧语义一致（双侧实测相同）。

自动化实现位于同目录 `FrameTest.java`：`check/checkThrows` 计数断言，`main` 打印 `PASS (N assertions)`；@key headful（DISPLAY=:77），通过 83 断言。
