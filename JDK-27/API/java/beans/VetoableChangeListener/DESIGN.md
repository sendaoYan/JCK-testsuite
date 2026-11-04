# java.beans.VetoableChangeListener — 设计案

## 1. API 功能
受约束属性监听器接口（extends EventListener），单一方法 `vetoableChange(PropertyChangeEvent) throws PropertyVetoException`。

## 2. Java 用户如何使用
监听器在属性变更前检查并抛 `PropertyVetoException` 拒绝变更。

## 3. 开源代码实际用法调研
JDK 源码 `VetoableChangeListener.java`；`VetoableChangeSupport` 事件模型。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends EventListener。黑盒（`testBlackBox`）：匿名实现验证 vetoableChange 分发。
