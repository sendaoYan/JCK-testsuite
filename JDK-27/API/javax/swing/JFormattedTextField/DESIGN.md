# javax.swing.JFormattedTextField 设计案

## 1. API 功能
`JFormattedTextField` 是带格式校验的文本字段（继承 `JTextField`）。方法分组：
- 值：`setValue/getValue`。
- 焦点丢失行为：`setFocusLostBehavior/getFocusLostBehavior` + 常量 `COMMIT/COMMIT_OR_REVERT/REVERT/PERSIST`。
- 校验：`commitEdit/isEditValid`。

## 2. Java 用户如何使用
用户用格式化字段输入数值/日期：
```java
JFormattedTextField field = new JFormattedTextField(NumberFormat.getIntegerInstance());
field.setValue(123);
field.commitEdit();
```
典型链路：`new JFormattedTextField(format)` → `setValue` → `commitEdit/isEditValid`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JFormattedTextField.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JFormattedTextField.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JTextField`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认值/焦点丢失行为、`setValue/getValue`、常量、对象/格式化构造、`NumberFormat` 格式化文本、非法文本 `commitEdit` 抛 `ParseException` 与 `isEditValid`。
