# javax.swing.text.AsyncBoxView — DESIGN

## 1. API 功能
异步布局的盒状视图（`extends View`）。核心方法：
- 轴：`getMajorAxis()/getMinorAxis()`。
- 内边距：`getTopInset/getBottomInset/getLeftInset/getRightInset` + set 往返、`getInsetSpan(axis)`（protected）。
- 子视图：`getViewCount()/getView(int)/getChildAllocation(int,Shape)/replace(int,int,View[])`、`getViewIndex(pos,bias)`。
- 尺寸：`getPreferredSpan/getMinimumSpan/getMaximumSpan`（主轴= majorSpan+inset；次轴未知时 margin+30 / insets+5 / MAX_VALUE）。
- 布局队列：`getLayoutQueue()`（LayoutQueue 默认队列）、`createChildState/getChildState`（protected）。
- 内部类：`ChildLocator`（子视图定位）、`ChildState`（子布局状态，implements Runnable）、`FlushTask`。

## 2. Java 用户如何使用
`new AsyncBoxView(element, View.X_AXIS)` 后设置内边距，`replace` 批量加入子视图触发异步布局；空视图（无子视图）查询主轴/次轴跨度不触发布局线程。子类可重写 `createChildState/getLayoutQueue`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/ZoneView.java:382`：`class Zone extends AsyncBoxView`，利用异步布局按需加载分区。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AsyncBoxView.java`：自身 `ChildState.run()` 读写文档读锁并更新 child 尺寸。

## 4. 测试用例设计（黑盒部分，`AsyncBoxViewTest.testBlackBox`）
1. 根元素构造：`getMajorAxis==X_AXIS/getMinorAxis==Y_AXIS`、offsets。
2. 空视图：`getViewCount==0/getView(0)==null`。
3. 内边距默认 0、set 往返。
4. 空视图跨度：主轴 preferred==majorSpan(0)+left+right(7)、次轴 preferred==margin(3)+30、次轴 minimum==insets(3)+5、次轴 maximum==Integer.MAX_VALUE。
