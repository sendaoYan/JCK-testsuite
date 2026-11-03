# javax.swing.DefaultListModel 设计案

## 1. API 功能
`DefaultListModel<E>` 是 `ListModel<E>` 的默认实现（内部用 `Vector`），提供完整动态列表操作。方法分组：
- 查询：`getSize/size/isEmpty/getElementAt/elementAt/firstElement/lastElement`。
- 搜索：`contains/indexOf/lastIndexOf`。
- 修改：`addElement/insertElementAt/removeElementAt/removeElement/removeAllElements/setElementAt/setSize`。
- 转换：`elements/toArray/copyInto/trimToSize/ensureCapacity/capacity`。

## 2. Java 用户如何使用
用户把它作为 JList 的数据模型，动态增删条目：
```java
DefaultListModel<String> model = new DefaultListModel<>();
model.addElement("one");
model.addElement("two");
list.setModel(model);
```
典型链路：`new DefaultListModel` → `addElement` → `insertElementAt` → `getElementAt` → `removeElement` → `size/isEmpty`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JList.java` 默认使用 `DefaultListModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JList.java`）。
- JDK 测试 `test/jdk/javax/swing/JList/` 大量使用 `DefaultListModel` 增删条目。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractListModel`、无接口。
- 行为（黑盒）：`testBlackBox()` 覆盖空列表、`addElement/getSize/getElementAt/firstElement/lastElement`、`indexOf/lastIndexOf/contains`、`insertElementAt/setElementAt/removeElementAt/removeElement`、`elements/toArray/copyInto`、`clear`。
