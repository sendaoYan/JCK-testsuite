# javax.swing.DefaultListCellRenderer 设计案

## 1. API 功能
`DefaultListCellRenderer` 是 `ListCellRenderer` 的默认实现，继承 `JLabel`，把单元格值显示为文本/图标。方法分组：
- 渲染：`getListCellRendererComponent(JList, value, index, isSelected, hasFocus)`。
- 覆盖自 JComponent：`isOpaque/validate/invalidate/repaint/revalidate/firePropertyChange` 等（优化渲染路径）。
- 继承自 JLabel：`setText/getText/setIcon` 等。

## 2. Java 用户如何使用
用户把它作为列表默认渲染器，或继承定制：
```java
JList<String> list = new JList<>(...);
list.setCellRenderer(new DefaultListCellRenderer());
```
典型链路：`new DefaultListCellRenderer` → `getListCellRendererComponent(list, value, index, selected, focus)` → 返回自身并设置文本。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JList.java` 默认使用 `DefaultListCellRenderer`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JList.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicListUI.java` 使用该渲染器绘制列表单元格。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JLabel`、实现 `ListCellRenderer`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `getListCellRendererComponent` 返回自身、文本从 value 设置、默认 enabled、非 opaque。
