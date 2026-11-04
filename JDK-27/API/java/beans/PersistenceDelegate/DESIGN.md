# java.beans.PersistenceDelegate — 设计案

## 1. API 功能
持久化委托抽象类。abstract `writeObject(oldInstance, Encoder)`；`mutatesTo`/`initialize` 描述对象如何构造。

## 2. Java 用户如何使用
为无默认构造器的类实现 PersistenceDelegate，供 XMLEncoder 序列化。

## 3. 开源代码实际用法调研
JDK 源码 `PersistenceDelegate.java`；`java.beans.*` 内置众多委托实现。

## 4. 测试用例设计 + 自动化实现
白盒：writeObject 抽象。黑盒（`testBlackBox`）：子类 writeObject 调用、mutatesTo 可调用。
