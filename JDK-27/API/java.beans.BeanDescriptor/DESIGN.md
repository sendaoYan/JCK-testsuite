# java.beans.BeanDescriptor — 设计案

## 1. API 功能
Bean 描述符（extends FeatureDescriptor）。构造器 `(beanClass)` / `(beanClass, customizerClass)`；getBeanClass/getCustomizerClass。

## 2. Java 用户如何使用
`BeanInfo.getBeanDescriptor()` 返回 Bean 的类及定制器（Customizer）。

## 3. 开源代码实际用法调研
JDK 源码 `BeanDescriptor.java`；`SimpleBeanInfo`/工具生成 BeanInfo。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：两个构造器的 beanClass/customizerClass 断言。
