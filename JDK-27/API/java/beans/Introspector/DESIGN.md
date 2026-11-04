# java.beans.Introspector — 设计案

## 1. API 功能
Bean 内省工具类。decapitalize、getBeanInfo（多重载）、get/setBeanInfoSearchPath、flushCaches/flushFromCaches。

## 2. Java 用户如何使用
`Introspector.getBeanInfo(Bean.class)` 返回 BeanInfo（含 PropertyDescriptor/EventSetDescriptor/MethodDescriptor）；`decapitalize` 将类名转属性名。

## 3. 开源代码实际用法调研
JDK 源码 `Introspector.java`；Spring/Guava 广泛使用 getBeanInfo 与 decapitalize。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：decapitalize 常规/首字母缩写、getBeanInfo 三种重载、flush 系列。
