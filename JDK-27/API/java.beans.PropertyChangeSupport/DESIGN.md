# java.beans.PropertyChangeSupport — 设计案

## 1. API 功能
属性变更支持类（implements Serializable）。构造器 `(sourceBean)`；add/removePropertyChangeListener（全局或按属性名）、getPropertyChangeListeners、firePropertyChange（String/Event + int/boolean 重载）、fireIndexedPropertyChange、hasListeners。

## 2. Java 用户如何使用
Bean 持有 `PropertyChangeSupport pcs = new PropertyChangeSupport(this)`；setter 中 `pcs.firePropertyChange("x", old, new)`；外部 `addPropertyChangeListener`。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyChangeSupport.java`；`java.awt.Component`/Swing 组件广泛使用，Spring 的 `PropertyChangeSupport` 用法同源。

## 4. 测试用例设计 + 自动化实现
白盒：implements Serializable。黑盒（`testBlackBox`）：全局/按名监听、fire 命中、indexed 触发、remove、hasListeners、Event 重载。
