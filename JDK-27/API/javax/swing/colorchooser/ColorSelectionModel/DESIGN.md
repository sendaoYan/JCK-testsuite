# javax.swing.colorchooser.ColorSelectionModel 设计案

## 1. API 功能
颜色选择模型接口，管理当前选中色与 `ChangeListener` 通知（`DefaultColorSelectionModel` 是默认实现）。

- **方法**：`Color getSelectedColor()`、`void setSelectedColor(Color)`、`void addChangeListener(ChangeListener)`、`void removeChangeListener(ChangeListener)`。

## 2. Java 用户如何使用
```java
ColorSelectionModel model = new DefaultColorSelectionModel(Color.blue);
model.setSelectedColor(Color.red);
model.addChangeListener(l -> { /* 颜色变化 */ });
```

典型组合链：`new 实现 → getSelectedColor → setSelectedColor → addChangeListener → setSelectedColor（触发 stateChanged）→ removeChangeListener`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/JColorChooser.java`：`private JComponent previewPanel = ColorChooserComponentFactory.getPreviewPanel();` 之外，`getSelectionModel()` 返回 `ColorSelectionModel`，`setColor()` 调用 `selectionModel.setSelectedColor(color)`。
- `src/java.desktop/share/classes/javax/swing/colorchooser/DefaultColorSelectionModel.java`：`public class DefaultColorSelectionModel implements ColorSelectionModel, Serializable`。

## 4. 测试用例设计 + 自动化实现
`ColorSelectionModelTest.testBlackBox()` 覆盖：
- 最小实现经接口引用的 `getSelectedColor`/`setSelectedColor` 往返。
- `addChangeListener` 后 `setSelectedColor` 触发 `stateChanged` 且事件源为模型；`removeChangeListener` 后不再触发。
- 白盒保留结构断言（接口、无父类、4 个方法），并抽查各方法存在。
