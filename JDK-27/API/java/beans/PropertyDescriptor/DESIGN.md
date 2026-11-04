# java.beans.PropertyDescriptor — 设计案

## 1. API 功能
属性描述符（extends FeatureDescriptor）。构造器 `(propertyName, beanClass)` / 显式读写方法等；getPropertyType/getReadMethod/getWriteMethod/isBound/isConstrained/getPropertyEditorClass。

## 2. Java 用户如何使用
`new PropertyDescriptor("foo", Bean.class)` 从 getter/setter 推导属性类型；`isBound` 判断是否绑定属性。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyDescriptor.java`；`Introspector` 构造之，Spring `BeanWrapper` 同源。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：int/boolean 属性推导、read/writeMethod、bound/constrained、显式方法对构造器、propertyEditorClass。
