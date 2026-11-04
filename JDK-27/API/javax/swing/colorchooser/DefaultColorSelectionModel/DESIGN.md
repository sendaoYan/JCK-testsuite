# javax.swing.colorchooser.DefaultColorSelectionModel 设计案

## 1. API 功能
`ColorSelectionModel` 的默认实现，维护选中色并以 `EventListenerList` 分发 `ChangeEvent`。

- **构造器**：`DefaultColorSelectionModel()`（默认 `Color.white`）、`DefaultColorSelectionModel(Color)`。
- **模型**：`getSelectedColor()`、`setSelectedColor(Color)`（同色或 `null` 不触发事件）。
- **监听器**：`addChangeListener`/`removeChangeListener`/`getChangeListeners()`（快照拷贝）、`fireStateChanged()`（protected）。
- 成员：`listenerList`、`changeEvent`（transient 复用）。

## 2. Java 用户如何使用
```java
DefaultColorSelectionModel model = new DefaultColorSelectionModel(Color.red);
model.getSelectedColor();       // Color.red
model.setSelectedColor(Color.green);
model.addChangeListener(l -> { /* 颜色变化 */ });
model.setSelectedColor(Color.blue); // 触发一次
```

典型组合链：`new(色) → getSelectedColor → setSelectedColor(同色，不触发) → addChangeListener → setSelectedColor(异色，触发一次) → getChangeListeners → removeChangeListener`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/JColorChooser.java`：`this(new DefaultColorSelectionModel(initialColor));` 与 `getSelectionModel()`/`setColor()` 使用该模型。
- `src/java.desktop/share/classes/javax/swing/colorchooser/DefaultColorSelectionModel.java`：`setSelectedColor` 实现为 `if (color != null && !selectedColor.equals(color)) { selectedColor = color; fireStateChanged(); }`。

## 4. 测试用例设计 + 自动化实现
`DefaultColorSelectionModelTest.testBlackBox()` 覆盖：
- 默认构造选白色、显式构造选指定色。
- `setSelectedColor` 更新选中色；同色不触发、异色触发一次且事件源为模型。
- `null` 颜色被忽略（不 NPE、不变、不触发）。
- `getChangeListeners` 列出监听器并返回快照拷贝；`removeChangeListener` 停止通知。
- 多监听器均被通知。
- 白盒保留结构断言（父类 `Object`、实现 `ColorSelectionModel`+`Serializable`、`getChangeListeners` 存在）。
