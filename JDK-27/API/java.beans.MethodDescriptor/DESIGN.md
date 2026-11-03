# java.beans.MethodDescriptor — 设计案

## 1. API 功能
方法描述符（extends FeatureDescriptor）。构造器 `(method)` / `(method, parameterDescriptors)`；getMethod/getParameterDescriptors。

## 2. Java 用户如何使用
`BeanInfo.getMethodDescriptors()` 返回公开方法列表及其参数描述。

## 3. 开源代码实际用法调研
JDK 源码 `MethodDescriptor.java`；`Introspector` 构造之。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：两个构造器的 method 与 parameterDescriptors。
