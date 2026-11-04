# javax.swing.DefaultBoundedRangeModel 设计案

## 1. API 功能
`DefaultBoundedRangeModel` 是 `BoundedRangeModel` 的默认实现（用于 JScrollBar/JSlider/JProgressBar 的范围状态）。方法分组：
- 构造：`DefaultBoundedRangeModel()`、`DefaultBoundedRangeModel(value, extent, min, max)`。
- 查询：`getValue/getExtent/getMinimum/getMaximum`。
- 修改：`setValue`、`setExtent`、`setMinimum`、`setMaximum`、`setRangeProperties`、`setValueIsAdjusting`。
- 监听：`addChangeListener`、`removeChangeListener`、`getChangeListeners`、`getListeners(Class)`。

## 2. Java 用户如何使用
用户把它绑到进度条/滚动条：
```java
DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(50, 10, 0, 100);
progressBar.setModel(m);
m.setValue(75);
```
典型链路：`new DefaultBoundedRangeModel(v,e,min,max)` → `setValue` → `setExtent` → `setRangeProperties` → `getValue`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JProgressBar.java`、`JScrollBar.java`、`JSlider.java` 默认使用 `DefaultBoundedRangeModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/`）。
- JDK 测试 `test/jdk/javax/swing/JScrollBar/`、`JSlider/` 使用 `DefaultBoundedRangeModel`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `BoundedRangeModel`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认零值、4 参构造、`setValue/setExtent/setMinimum/setMaximum`、`setValueIsAdjusting`、`setRangeProperties` 一次性修改、`addChangeListener` 触发 `stateChanged`。
