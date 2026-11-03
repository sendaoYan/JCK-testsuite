# javax.swing.JViewport 黑盒行为测试设计

## 1. API 功能
JViewport 是滚动窗口的"取景器"，承载单个轻量级 view，继承 JComponent，实现 Accessible。

- 滚动模式常量：`BLIT_SCROLL_MODE=1`、`BACKINGSTORE_SCROLL_MODE=2`、`SIMPLE_SCROLL_MODE=0`；`setScrollMode/getScrollMode`、`isBackingStoreEnabled/setBackingStoreEnabled`（后两者已弃用，映射到 scrollMode）。
- 视图：`getView/setView(Component)`（单子组件）、`getViewSize/setViewSize`、`getViewPosition/setViewPosition`、`getViewRect`。
- 可见区：`getExtentSize/setExtentSize`。
- 坐标转换：`toViewCoordinates(Point/Dimension)`（基础实现为恒等拷贝）。
- 滚动：`scrollRectToVisible(Rectangle)`。
- 边框约束：`setBorder`（非 null 抛 IAE）、`getInsets`（恒为 0）。
- UI：`getUIClassID()` 返回 "ViewportUI"。

## 2. Java 用户如何使用
- `JViewport vp = new JViewport(); vp.setView(bigPanel);` 设置被滚动内容。
- `vp.setViewSize(new Dimension(2000, 1000));` 设定视图尺寸。
- `vp.setViewPosition(new Point(100, 200));` 滚动到指定视图坐标。
- `vp.getViewRect()` 得到当前可见区（origin=viewPosition, size=extentSize）。
- `vp.scrollRectToVisible(rect)` 程序化滚动使矩形可见。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/javax/swing/JScrollPane/6612531/bug6612531.java` 直接操作 JViewport。
- `test/jdk/javax/swing/UI/UnninstallUIMemoryLeaks/UnninstallUIMemoryLeaks.java` 覆盖 viewport 卸载。
- JScrollPane 内部 `createViewport()` 返回 JViewport 并作为 view 容器。
以上来源为 `~/git/jdk25u-dev/test/jdk` 与 `src/java.desktop/share/classes/javax/swing/JScrollPane.java`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JViewportTest.testBlackBox()`）：
1. 空视口默认：view null / viewSize (0,0) / viewPosition (0,0) / extentSize (0,0) / BLIT 模式 / backingStore false / 非优化绘制 / getUIClassID。
2. setExtentSize 往返、三种 scrollMode 往返、setBackingStoreEnabled 映射。
3. setView/getView、setViewSize/getViewSize、setViewPosition/getViewPosition、getViewRect。
4. toViewCoordinates(Point/Dimension)。
5. setBorder(null) 允许 / 非 null 抛 IAE；getInsets 恒零。
6. scrollRectToVisible 空视口与有 view 均不抛。
7. setView(null) 清空视图。
