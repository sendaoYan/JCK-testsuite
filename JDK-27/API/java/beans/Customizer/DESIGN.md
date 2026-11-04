# java.beans.Customizer — 设计案

## 1. API 功能
Bean 定制器接口。setObject/addPropertyChangeListener/removePropertyChangeListener。

## 2. Java 用户如何使用
实现 Customizer 提供属性面板；BeanDescriptor.getCustomizerClass 指定之。

## 3. 开源代码实际用法调研
JDK 源码 `Customizer.java`；IDE 定制器。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现 setObject 分发。
