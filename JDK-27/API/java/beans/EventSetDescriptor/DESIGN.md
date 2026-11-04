# java.beans.EventSetDescriptor — 设计案

## 1. API 功能
事件集描述符（extends FeatureDescriptor）。getListenerType/getListenerMethods/getAddListenerMethod/getRemoveListenerMethod/isUnicast/isInDefaultEventSet。

## 2. Java 用户如何使用
`new EventSetDescriptor(Source.class, "foo", FooListener.class, "fooHappened")` 描述事件集；IDE/工具据此生成监听器代码。

## 3. 开源代码实际用法调研
JDK 源码 `EventSetDescriptor.java`；`Introspector` 构造之。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：构造器推导 listener type/add/remove/listenerMethods 及 unicast/inDefaultEventSet。
