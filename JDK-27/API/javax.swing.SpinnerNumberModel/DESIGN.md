# javax.swing.SpinnerNumberModel 设计案

## 1. API 功能
`SpinnerNumberModel` 是数值步进模型（继承 `AbstractSpinnerModel`）。方法分组：
- 范围：`setMinimum/getMinimum/setMaximum/getMaximum/setStepSize/getStepSize`。
- 值：`getNumber/getValue/setValue/getNextValue/getPreviousValue`。

## 2. Java 用户如何使用
用户用 `JSpinner` 选择数值：
```java
SpinnerNumberModel model = new SpinnerNumberModel(5, 0, 10, 1);
int next = ((Number) model.getNextValue()).intValue();
```
典型链路：构造 → `getNextValue/getPreviousValue` → `setStepSize`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SpinnerNumberModel.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SpinnerNumberModel.java`）。
- `JSpinner.NumberEditor` 使用该模型编辑数值。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractSpinnerModel` + `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖范围/步长 get/set、`getNextValue/getPreviousValue` 步进、`setValue` 不夹取（经源码验证）、默认与 double 构造。
