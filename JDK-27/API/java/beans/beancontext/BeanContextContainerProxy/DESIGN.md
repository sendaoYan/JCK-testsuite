# java.beans.beancontext.BeanContextContainerProxy — 设计案

## 1. API 功能
接口。`Container getContainer()` 返回 BeanContext 关联的 AWT 容器。

## 2. Java 用户如何使用
实现该接口的 BeanContext 暴露其容器供子组件挂载。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextContainerProxy.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现返回 Panel 的 getContainer。
