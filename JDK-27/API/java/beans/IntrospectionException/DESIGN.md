# java.beans.IntrospectionException — 设计案

## 1. API 功能
内省异常（extends Exception）。构造器 `(String)`。

## 2. Java 用户如何使用
`Introspector.getBeanInfo` 失败时抛 IntrospectionException。

## 3. 开源代码实际用法调研
JDK 源码 `IntrospectionException.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends Exception。黑盒（`testBlackBox`）：message 与 null message。
