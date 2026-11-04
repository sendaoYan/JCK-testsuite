# javax.swing.text.AsyncBoxView.ChildState — DESIGN

## 1. API 功能
异步盒视图的子视图布局状态记录（内部 public 类，`implements Runnable`）。核心方法：
- 构造：`ChildState(View)`（将 child 的 parent 设为所属 AsyncBoxView）。
- 访问：`getChildView()`；跨度/偏移：`getMajorSpan()/getMajorOffset()/getMinorSpan()/getMinorOffset()/setMajorOffset(float)`。
- 状态：`isLayoutValid()`（minor/major/childSize 三者有效）、`preferenceChanged(boolean,boolean)`。
- `run()`：获取文档读锁后 `updateChild` 更新 min/pref/max 与尺寸（布局线程任务）。

## 2. Java 用户如何使用
由 AsyncBoxView 内部创建（`createChildState`），测试通过 `asyncBoxView.new ChildState(child)` 构造并断言初始零跨度/零偏移、`isLayoutValid()==false`、`setMajorOffset` 往返。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AsyncBoxView.java:1273`：`ChildState.run()` 中 `doc.readLock()` 后 `updateChild`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AsyncBoxView.java:1321`：`getMinorSpan` 在 `max < minorSpan` 时返回 max。

## 4. 测试用例设计（黑盒部分，`ChildStateTest.testBlackBox`）
1. 构造后 `getChildView()==child`、`child.getParent()==box`（构造即 parent）。
2. 初始：`isLayoutValid()==false`、`getMajorSpan()==0`、`getMajorOffset()==0`、`getMinorSpan()==0`、`getMinorOffset()==0`（零对齐 child）。
3. `setMajorOffset(42)` 往返。
4. `preferenceChanged(true,true)` 不抛且仍 `isLayoutValid()==false`。
