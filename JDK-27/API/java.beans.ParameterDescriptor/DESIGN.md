# java.beans.ParameterDescriptor — 设计案

## 1. API 功能
方法参数描述符（extends FeatureDescriptor），无特有方法，仅提供命名。

## 2. Java 用户如何使用
`MethodDescriptor` 的参数数组中每项一个 ParameterDescriptor，用于参数命名。

## 3. 开源代码实际用法调研
JDK 源码 `ParameterDescriptor.java`；`Introspector` 参数描述。

## 4. 测试用例设计 + 自动化实现
白盒：extends FeatureDescriptor。黑盒（`testBlackBox`）：继承的 name/shortDescription 行为。
