# java.beans.PropertyChangeEvent — 设计案

## 1. API 功能
属性变更事件（extends EventObject）。构造器 `(source, propertyName, oldValue, newValue)`；getPropertyName/getOldValue/getNewValue/getPropagationId/setPropagationId/toString。

## 2. Java 用户如何使用
`new PropertyChangeEvent(source, "name", old, new)`；监听器通过 getPropertyName 判断属性、getNewValue/getOldValue 取新旧值。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyChangeEvent.java`；`PropertyChangeSupport.firePropertyChange` 构造之，Spring/Guava 广泛使用。

## 4. 测试用例设计 + 自动化实现
白盒：extends EventObject。黑盒（`testBlackBox`）：构造器四字段精确断言 + propagationId 缺省/设置 + toString。
