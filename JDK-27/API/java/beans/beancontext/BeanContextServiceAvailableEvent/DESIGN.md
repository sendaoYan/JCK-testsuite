# java.beans.beancontext.BeanContextServiceAvailableEvent — 设计案

## 1. API 功能
服务可用事件（extends BeanContextEvent）。构造器 `(BeanContextServices, Class)`；getSourceAsBeanContextServices/getServiceClass/getCurrentServiceSelectors。

## 2. Java 用户如何使用
serviceAvailable 监听器接收该事件，查询可用的服务与选择器。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServiceAvailableEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends BeanContextEvent。黑盒（`testBlackBox`）：构造器及 getSourceAsBeanContextServices/getServiceClass/getCurrentServiceSelectors。
