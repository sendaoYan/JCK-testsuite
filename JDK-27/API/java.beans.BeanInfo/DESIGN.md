# java.beans.BeanInfo — 设计案

## 1. API 功能
Bean 信息接口。常量 ICON_*、USE_ALL_BEANINFO；getBeanDescriptor/getPropertyDescriptors/getEventSetDescriptors/getMethodDescriptors/getDefaultPropertyIndex/getDefaultEventIndex/getIcon/getAdditionalBeanInfo。

## 2. Java 用户如何使用
自定义 BeanInfo 描述 Bean 属性/事件/方法/图标；Introspector 返回。

## 3. 开源代码实际用法调研
JDK 源码 `BeanInfo.java`；JavaBeans 规范核心接口。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 常量 + 关键方法。黑盒（`testBlackBox`）：SimpleBeanInfo 默认值。
