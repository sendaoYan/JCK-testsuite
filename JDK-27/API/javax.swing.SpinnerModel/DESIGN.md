# javax.swing.SpinnerModel 设计案

## 1. API 功能
`SpinnerModel` 是步进模型接口。方法分组：
- 值：`getValue/setValue/getNextValue/getPreviousValue`。
- 监听：`addChangeListener/removeChangeListener`。

## 2. Java 用户如何使用
`JSpinner` 依赖该模型提供值序列：
```java
SpinnerModel model = new SpinnerNumberModel(0, 0, 10, 1);
int next = ((Number) model.getNextValue()).intValue();
```
典型链路：`getValue` → `getNextValue/getPreviousValue`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SpinnerModel.java` 定义该接口（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SpinnerModel.java`）。
- `SpinnerNumberModel/SpinnerDateModel/SpinnerListModel` 均实现它。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 通过 `SpinnerNumberModel` 覆盖 `getValue/setValue/getNextValue/getPreviousValue` 与监听器 add/remove。
