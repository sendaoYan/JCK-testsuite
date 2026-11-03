# java.beans.BeanProperty — 设计案

## 1. API 功能
注解（@Target METHOD/FIELD）。bound/expert/hidden/preferred/required/visualUpdate/description/enumerationValues。bound 默认 true，其余 boolean 默认 false，description 默认 ""。

## 2. Java 用户如何使用
`@BeanProperty(bound=true)` 标注 getter 声明绑定属性。

## 3. 开源代码实际用法调研
JDK 源码 `BeanProperty.java`；BeanInfo 生成。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型。黑盒（`testBlackBox`）：反射验证默认值（bound=true 等）与显式覆盖。
