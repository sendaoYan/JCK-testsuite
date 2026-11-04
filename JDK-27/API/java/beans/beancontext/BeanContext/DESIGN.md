# java.beans.beancontext.BeanContext — 设计案

## 1. API 功能
Bean 上下文接口（extends BeanContextChild, Collection, DesignMode, Visibility）。常量 globalHierarchyLock；instantiateChild/getResource/getResourceAsStream/addBeanContextMembershipListener/removeBeanContextMembershipListener。

## 2. Java 用户如何使用
Bean 容器实现该接口管理子 Bean：instantiateChild 动态加载子 Bean，getResource 按子 Bean 类加载器查资源，membership 监听子 Bean 增删。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContext.java`；beancontext 包内 BeanContextSupport 实现之。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + globalHierarchyLock + 四个父接口。黑盒（`testBlackBox`）：instantiateChild 实例化 String、membership 监听器 add/remove、getResourceAsStream/getResource 空参数 NPE。
