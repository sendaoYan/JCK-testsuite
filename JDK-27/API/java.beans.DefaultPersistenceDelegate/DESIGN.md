# java.beans.DefaultPersistenceDelegate — 设计案

## 1. API 功能
默认持久化委托（extends PersistenceDelegate）。构造器 `()` 与 `(String[] constructorPropertyNames)`；用默认构造器 + 属性 setter 重建对象。

## 2. Java 用户如何使用
XMLEncoder 对满足 JavaBean 约定的类自动使用此委托。

## 3. 开源代码实际用法调研
JDK 源码 `DefaultPersistenceDelegate.java`；XMLEncoder 默认委托。

## 4. 测试用例设计 + 自动化实现
白盒：extends PersistenceDelegate。黑盒（`testBlackBox`）：两个构造器实例化。
