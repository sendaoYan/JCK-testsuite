# javax.swing.JSplitPane 黑盒行为测试设计

## 1. API 功能
JSplitPane 是双面板分隔容器，继承 JComponent，实现 Accessible。

- 常量：`HORIZONTAL_SPLIT=1`、`VERTICAL_SPLIT=0`，及 `LEFT/RIGHT/TOP/BOTTOM/DIVIDER` 与 `*_PROPERTY` 字符串常量。
- 构造器：`JSplitPane()`、`JSplitPane(int orientation)`、`JSplitPane(int, boolean continuousLayout)`、`JSplitPane(int, Component, Component)`、`JSplitPane(int, boolean, Component, Component)`；非法方向抛 IAE。
- 方向：`getOrientation/setOrientation`。
- 子组件：`getLeftComponent/setLeftComponent`、`getRightComponent/setRightComponent`、`getTopComponent/setTopComponent`（映射 left）、`getBottomComponent/setBottomComponent`（映射 right）。
- 分隔条：`getDividerSize/setDividerSize`、`getDividerLocation/setDividerLocation(int)`、`setDividerLocation(double)`（0..1 外抛 IAE）、`getMinimumDividerLocation/getMaximumDividerLocation`、`getLastDividerLocation/setLastDividerLocation`。
- 行为：`isContinuousLayout/setContinuousLayout`、`isOneTouchExpandable/setOneTouchExpandable`、`getResizeWeight/setResizeWeight`、`resetToPreferredSizes`。
- UI：`getUIClassID()` 返回 "SplitPaneUI"。

## 2. Java 用户如何使用
- `JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);` 左右布局。
- `sp.setDividerLocation(200);` 固定分隔位置；`sp.setResizeWeight(0.5);` 平分额外空间。
- `sp.setOneTouchExpandable(true);` 显示一键收起按钮。
- `sp.setContinuousLayout(true);` 拖动时实时刷新。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/java/awt/Mixing/AWT_Mixing/JSplitPaneOverlapping.java` 用 JSplitPane 做重叠混合绘制。
- `test/jdk/java/awt/Component/DimensionEncapsulation/*` 遍历 JSplitPane 验证尺寸封装。
- `test/jdk/java/awt/regtesthelpers/PassFailJFrame.java` 用 JSplitPane 作为测试脚手架布局。
以上来源为 `~/git/jdk25u-dev/test/jdk`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JSplitPaneTest.testBlackBox()`）：
1. 默认构造：HORIZONTAL_SPLIT / 左右默认按钮 / resizeWeight 0.0 / getUIClassID。
2. 方向构造器、组件构造器（左右组件往返）、continuousLayout 构造器。
3. 非法方向 IAE。
4. setLeftComponent/setRightComponent/setTopComponent/setBottomComponent 往返（top/bottom 映射 left/right）。
5. setDividerSize 往返 + 负数忽略。
6. setOneTouchExpandable/setLastDividerLocation/setResizeWeight/setContinuousLayout 往返。
7. setDividerLocation(int) 往返；setDividerLocation(double) 越界 IAE。
8. resetToPreferredSizes 不抛；setLeftComponent(null)/setRightComponent(null) 清空。
