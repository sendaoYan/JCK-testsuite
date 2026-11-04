# java.beans.beancontext.BeanContextSupport — 设计案

## 1. API 功能
BeanContext 实现基类（implements BeanContext, Serializable）。构造器 `(peer, locale, designTime, okToUseGui)` 系列；size/isEmpty/contains/containsKey/add/remove/addAll/removeAll/retainAll/clear/iterator/toArray；add/removeBeanContextMembershipListener、getResource/getResourceAsStream、instantiateChild、setDesignTime/isDesignTime、setLocale/getLocale、needsGui/dontUseGui/okToUseGui/avoidingGui、isSerializing、getBeanContextPeer。

## 2. Java 用户如何使用
直接实例化或继承作为 Bean 容器；add 添加子 Bean，成员监听器接收增删事件。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextSupport.java`；beancontext 包核心实现。

## 4. 测试用例设计 + 自动化实现
白盒：implements BeanContext + Serializable。黑盒（`testBlackBox`）：集合操作、designTime/locale、gui 可见性、instantiateChild、成员事件触发、构造器。
