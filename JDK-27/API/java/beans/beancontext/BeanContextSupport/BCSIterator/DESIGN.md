# java.beans.beancontext.BeanContextSupport.BCSIterator — 设计案

## 1. API 功能
BCSIterator 嵌套 final 类（implements Iterator）。hasNext/next/remove。

## 2. Java 用户如何使用
BeanContextSupport.iterator() 返回之，用于遍历子 Bean。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：final + Iterator。黑盒（`testBlackBox`）：iterator() 返回 BCSIterator，hasNext/next/remove。
