# java.beans.ConstructorProperties — 设计案

## 1. API 功能
注解（@Target CONSTRUCTOR）。`String[] value()` 声明构造器参数名。

## 2. Java 用户如何使用
`@ConstructorProperties({"a","b"})` 标注构造器，供 Introspector/序列化识别参数名。

## 3. 开源代码实际用法调研
JDK 源码 `ConstructorProperties.java`；Jackson/Gson 读取参数名。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型 + value 返回 String[]。黑盒（`testBlackBox`）：反射读取注解 value。
