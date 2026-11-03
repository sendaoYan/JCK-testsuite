# java.beans.JavaBean — 设计案

## 1. API 功能
注解（@Target TYPE）。description/defaultProperty/defaultEventSet（默认均 ""）。

## 2. Java 用户如何使用
`@JavaBean(description="...", defaultProperty="x")` 声明 Bean 元信息。

## 3. 开源代码实际用法调研
JDK 源码 `JavaBean.java`；BeanInfo 生成。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型。黑盒（`testBlackBox`）：反射读取三个属性。
