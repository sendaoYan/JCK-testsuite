# java.beans.SimpleBeanInfo — 设计案

## 1. API 功能
BeanInfo 便利空实现（implements BeanInfo），所有 getter 返回 null 或 -1。

## 2. Java 用户如何使用
继承 SimpleBeanInfo 只覆盖需要的 getter。

## 3. 开源代码实际用法调研
JDK 源码 `SimpleBeanInfo.java`；用户自定义 BeanInfo 常继承之。

## 4. 测试用例设计 + 自动化实现
白盒：implements BeanInfo。黑盒（`testBlackBox`）：各 getter 默认 null/-1 断言。
