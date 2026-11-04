# java.beans.DesignMode — 设计案

## 1. API 功能
设计模式接口。常量 PROPERTYNAME="designTime"；setDesignTime/isDesignTime。

## 2. Java 用户如何使用
BeanContext 等实现该接口区分设计期/运行期。

## 3. 开源代码实际用法调研
JDK 源码 `DesignMode.java`；beancontext 实现。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + PROPERTYNAME。黑盒（`testBlackBox`）：匿名实现 set/isDesignTime。
