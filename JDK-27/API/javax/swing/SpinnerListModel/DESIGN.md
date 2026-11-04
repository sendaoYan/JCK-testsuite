# javax.swing.SpinnerListModel 设计案

## 1. API 功能
`SpinnerListModel` 是列表步进模型（继承 `AbstractSpinnerModel`）。方法分组：
- 列表：`setList/getList`。
- 值：`getValue/setValue/getNextValue/getPreviousValue`。

## 2. Java 用户如何使用
用户用 `JSpinner` 在固定列表中循环选择：
```java
SpinnerListModel model = new SpinnerListModel(new String[] { "low", "medium", "high" });
String next = (String) model.getNextValue();
```
典型链路：构造 → `getNextValue/getPreviousValue`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SpinnerListModel.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SpinnerListModel.java`）。
- `JSpinner` 使用该模型做列表选择。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractSpinnerModel` + `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖初始值、首/尾元素处 `getNextValue/getPreviousValue` 返回 null（不环绕，经源码验证）、`setValue/setList/getList`、三种构造器。
