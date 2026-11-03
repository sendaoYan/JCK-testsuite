# java.beans.beancontext.BeanContextEvent — 设计案

## 1. API 功能
BeanContext 事件抽象基类（extends EventObject，protected 构造器）。getBeanContext/getPropagatedFrom/setPropagatedFrom/isPropagated。

## 2. Java 用户如何使用
所有 beancontext 事件的基类；isPropagated 表示事件从嵌套上下文传播而来。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends EventObject + abstract。黑盒（`testBlackBox`）：子类 getBeanContext、propagatedFrom 默认/set/isPropagated。
