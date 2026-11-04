# java.beans.FeatureDescriptor — 设计案

## 1. API 功能
Bean 特性（属性/事件/方法）描述符基类。name/displayName/shortDescription/expert/hidden/preferred 及通用属性表 setValue/getValue/getAttributeNames。

## 2. Java 用户如何使用
`BeanDescriptor`/`PropertyDescriptor` 等继承；工具通过 getName/getShortDescription/isExpert 展示 Bean 特性。

## 3. 开源代码实际用法调研
JDK 源码 `FeatureDescriptor.java`；所有描述符类的共同基类。

## 4. 测试用例设计 + 自动化实现
白盒：非接口。黑盒（`testBlackBox`）：name/displayName/shortDescription/expert/hidden/preferred 及属性表 set/get/enumerate。
