# java.beans.VetoableChangeSupport — 设计案

## 1. API 功能
受约束属性变更支持类（implements Serializable）。add/removeVetoableChangeListener、getVetoableChangeListeners、fireVetoableChange（String/int/boolean/Event 重载，抛 PropertyVetoException）、hasListeners。

## 2. Java 用户如何使用
Bean setter 先 `vcs.fireVetoableChange("x", old, new)`，监听器可抛 PropertyVetoException 拒绝；通过后赋值。

## 3. 开源代码实际用法调研
JDK 源码 `VetoableChangeSupport.java`；JavaBeans 受约束属性事件模型。

## 4. 测试用例设计 + 自动化实现
白盒：implements Serializable。黑盒（`testBlackBox`）：add/remove/hasListeners、fire 命中、veto 异常传播与事件属性。
