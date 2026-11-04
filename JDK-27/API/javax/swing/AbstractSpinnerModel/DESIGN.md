# javax.swing.AbstractSpinnerModel 设计案

## 1. API 功能
`AbstractSpinnerModel` 是 `SpinnerModel` 的抽象基类，管理 `ChangeListener` 列表。方法分组：
- 值（抽象）：`getValue()`、`setValue(Object)`、`getNextValue()`、`getPreviousValue()`。
- 监听：`addChangeListener`、`removeChangeListener`、`getChangeListeners`、`getListeners(Class<T>)`。
- 通知（受保护）：`fireStateChanged()`。

## 2. Java 用户如何使用
用户继承它实现数值/日期/列表 spinner 模型，值变化时调用 `fireStateChanged` 通知 JSpinner：
```java
class MyModel extends AbstractSpinnerModel {
    public Object getValue() { return v; }
    public void setValue(Object o) { v = o; fireStateChanged(); }
    public Object getNextValue() { return v + 1; }
    public Object getPreviousValue() { return v - 1; }
}
```
典型链路：`new AbstractSpinnerModel(){...}` → `getValue` → `setValue` → `fireStateChanged` → `getChangeListeners`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SpinnerNumberModel.java`、`SpinnerDateModel.java`、`SpinnerListModel.java` 均 `extends AbstractSpinnerModel`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/`）。
- JDK 源码 `javax/swing/JSpinner.java` 通过 `SpinnerModel.addChangeListener` 订阅模型变更刷新编辑器。
- JDK 测试 `test/jdk/javax/swing/JSpinner/` 使用 `SpinnerNumberModel`（间接继承 `AbstractSpinnerModel`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、实现 `SpinnerModel`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖匿名子类的 `getValue/getNextValue/getPreviousValue`、`add/remove/getChangeListeners`、受保护的 `fireStateChanged` 触发 `stateChanged` 回调、`getListeners(Class)` 过滤。
