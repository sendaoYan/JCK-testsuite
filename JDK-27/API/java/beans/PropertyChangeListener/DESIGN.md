# java.beans.PropertyChangeListener — 设计案

## 1. API 功能
属性变更监听器接口（extends EventListener），单一方法 `propertyChange(PropertyChangeEvent)`。

## 2. Java 用户如何使用
实现 `propertyChange` 接收属性变更；`PropertyChangeSupport.addPropertyChangeListener` 注册。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyChangeListener.java`；JavaBeans/Swing 事件模型核心接口。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends EventListener。黑盒（`testBlackBox`）：匿名实现验证 propertyChange 分发。
