# java.beans.Expression — 设计案

## 1. API 功能
表达式（extends Statement），表示"目标对象调用方法"并缓存结果。构造器 `(target, methodName, arguments)` 与 `(value, target, methodName, arguments)`；getValue() 计算并缓存。

## 2. Java 用户如何使用
`new Expression("hi", "toUpperCase", args).getValue()` 求值；`XMLEncoder` 用其表示对象构造。

## 3. 开源代码实际用法调研
JDK 源码 `Expression.java`；PersistenceDelegate 生成之。

## 4. 测试用例设计 + 自动化实现
白盒：extends Statement。黑盒（`testBlackBox`）：目标/方法名、getValue 求值、预置 value 构造器。
