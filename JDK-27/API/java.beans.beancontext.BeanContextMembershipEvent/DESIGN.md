# java.beans.beancontext.BeanContextMembershipEvent — 设计案

## 1. API 功能
成员变更事件（extends BeanContextEvent）。构造器 `(BeanContext, Collection)` / `(BeanContext, Object[])`；size/contains/toArray/iterator。

## 2. Java 用户如何使用
childrenAdded/childrenRemoved 传入该事件，遍历新增/删除的子 Bean。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextMembershipEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends BeanContextEvent。黑盒（`testBlackBox`）：数组/集合构造器及 size/contains/toArray/iterator。
