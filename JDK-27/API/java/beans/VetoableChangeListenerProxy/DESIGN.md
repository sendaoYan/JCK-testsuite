# java.beans.VetoableChangeListenerProxy — 设计案

## 1. API 功能
受约束属性监听器代理（extends EventListenerProxy<VetoableChangeListener>）。构造器 `(propertyName, listener)`；getPropertyName/getListener/vetoableChange。

## 2. Java 用户如何使用
`new VetoableChangeListenerProxy("foo", listener)` 按属性名绑定受约束监听器。

## 3. 开源代码实际用法调研
JDK 源码 `VetoableChangeListenerProxy.java`；`VetoableChangeSupport` 内部使用。

## 4. 测试用例设计 + 自动化实现
白盒：extends EventListenerProxy + implements VetoableChangeListener。黑盒（`testBlackBox`）：构造器、getPropertyName/getListener、vetoableChange 委托。
