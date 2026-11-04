# java.beans.beancontext.BeanContextServiceRevokedEvent — 设计案

## 1. API 功能
服务撤销事件（extends BeanContextEvent）。构造器 `(BeanContextServices, Class, boolean)`；getSourceAsBeanContextServices/getServiceClass/isServiceClass/isCurrentServiceInvalidNow。

## 2. Java 用户如何使用
serviceRevoked 监听器接收该事件；isCurrentServiceInvalidNow 决定是否强制撤销。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServiceRevokedEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends BeanContextEvent。黑盒（`testBlackBox`）：构造器、isServiceClass、isCurrentServiceInvalidNow 两个值。
