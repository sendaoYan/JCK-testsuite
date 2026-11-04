# javax.swing.SpinnerDateModel 设计案

## 1. API 功能
`SpinnerDateModel` 是日期步进模型（继承 `AbstractSpinnerModel`）。方法分组：
- 范围：`setStart/getStart/setEnd/getEnd`。
- 日历字段：`setCalendarField/getCalendarField`。
- 值：`getDate/getValue/setValue/getNextValue/getPreviousValue`。

## 2. Java 用户如何使用
用户用 `JSpinner` 选择日期：
```java
SpinnerDateModel model = new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH);
Date next = (Date) model.getNextValue();
```
典型链路：构造 → `getNextValue/getPreviousValue` → `getDate`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SpinnerDateModel.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SpinnerDateModel.java`）。
- `JSpinner.DateEditor` 使用该模型编辑日期。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractSpinnerModel` + `Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `getDate/getCalendarField`、按日历字段 ±1 的 `getNextValue/getPreviousValue`、`setStart/setEnd/setCalendarField/setValue`。
