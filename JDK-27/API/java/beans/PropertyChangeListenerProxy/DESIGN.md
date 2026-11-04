# java.beans.PropertyChangeListenerProxy — 设计案

## 1. API 功能
属性变更监听器代理（extends EventListenerProxy<PropertyChangeListener>），按属性名绑定监听器。构造器 `(propertyName, listener)`；getPropertyName/getListener/propertyChange。

## 2. Java 用户如何使用
`new PropertyChangeListenerProxy("foo", listener)` 只接收 "foo" 属性事件。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyChangeListenerProxy.java`；`PropertyChangeSupport` 按属性名分组时内部使用代理。

## 4. 测试用例设计 + 自动化实现
白盒：extends EventListenerProxy + implements PropertyChangeListener。黑盒（`testBlackBox`）：构造器、getPropertyName/getListener、propertyChange 委托。
