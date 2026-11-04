# java.beans.beancontext.BeanContextChild — 设计案

## 1. API 功能
Bean 上下文子接口。setBeanContext/getBeanContext、按名 add/removePropertyChangeListener、按名 add/removeVetoableChangeListener。

## 2. Java 用户如何使用
子 Bean 实现该接口以挂载到 BeanContext；setBeanContext 受 vetoable 约束。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextChild.java`；BeanContextChildSupport 实现之。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：BeanContextChildSupport 的 setBeanContext/getBeanContext 往返。
