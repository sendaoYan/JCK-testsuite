# java.beans.PropertyVetoException — 设计案

## 1. API 功能
受约束属性否决异常（extends Exception）。构造器 `(mess, PropertyChangeEvent)`；getPropertyChangeEvent()。

## 2. Java 用户如何使用
`VetoableChangeListener` 抛 `new PropertyVetoException("reason", evt)` 拒绝属性变更。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyVetoException.java`；受约束属性事件模型核心异常。

## 4. 测试用例设计 + 自动化实现
白盒：extends Exception。黑盒（`testBlackBox`）：构造器 message 与 propertyChangeEvent。
