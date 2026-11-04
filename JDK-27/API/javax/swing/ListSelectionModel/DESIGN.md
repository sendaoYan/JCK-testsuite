# javax.swing.ListSelectionModel 设计案

## 1. API 功能
`ListSelectionModel` 接口描述列表选择状态模型。方法分组：
- 选择：`setSelectionInterval/addSelectionInterval/removeSelectionInterval/clearSelection`。
- 查询：`isSelectedIndex/getMinSelectionIndex/getMaxSelectionIndex/isSelectionEmpty/getSelectedIndices`。
- 模式：`setSelectionMode/getSelectionMode` + 三个常量。

## 2. Java 用户如何使用
用户通过 `JList.getSelectionModel()` 操作选择：
```java
ListSelectionModel model = list.getSelectionModel();
model.setSelectionInterval(0, 5);
```
典型链路：`setSelectionInterval` → `isSelectedIndex/getMinSelectionIndex`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultListSelectionModel.java` 是该接口默认实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultListSelectionModel.java`）。
- `JList`/`JTable` 选择模型均实现此接口。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 校验三个模式常量 + 通过 `DefaultListSelectionModel` 覆盖选择区间、选中查询、清空。
