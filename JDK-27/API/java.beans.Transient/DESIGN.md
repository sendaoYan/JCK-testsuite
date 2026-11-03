# java.beans.Transient — 设计案

## 1. API 功能
注解（@Target METHOD/FIELD）。`boolean value()` 默认 true，标记属性为 transient（不参与内省）。

## 2. Java 用户如何使用
`@Transient` 标注 getter 或字段，让 Introspector 忽略该属性。

## 3. 开源代码实际用法调研
JDK 源码 `Transient.java`；BeanInfo 生成。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型。黑盒（`testBlackBox`）：反射验证 value 默认 true 与 @Transient(false)。
