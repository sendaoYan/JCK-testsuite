# javax.swing.MutableComboBoxModel 设计案

## 1. API 功能
`MutableComboBoxModel` 接口在 `ComboBoxModel` 基础上支持可变数据。方法分组：
- 增删：`addElement/insertElementAt/removeElement/removeElementAt`。

## 2. Java 用户如何使用
用户用可变模型动态更新组合框选项：
```java
MutableComboBoxModel<String> model = new DefaultComboBoxModel<String>();
model.addElement("item");
model.insertElementAt("first", 0);
```
典型链路：`addElement` → `insertElementAt` → `removeElement/removeElementAt`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultComboBoxModel.java` 实现 `MutableComboBoxModel<E>`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultComboBoxModel.java`）。
- `JComboBox` 使用可变模型动态增删选项。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口继承 `ComboBoxModel`。
- 行为（黑盒）：`testBlackBox()` 通过 `DefaultComboBoxModel` 覆盖 `addElement/insertElementAt/removeElement/removeElementAt`。
