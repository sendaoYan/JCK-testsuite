# java.beans.Statement — 设计案

## 1. API 功能
语句，表示"目标对象调用方法"。构造器 `(target, methodName, arguments)`；getTarget/getMethodName/getArguments/execute()。

## 2. Java 用户如何使用
`new Statement(obj, "m", args).execute()` 反射调用；`Expression` 继承并缓存值。

## 3. 开源代码实际用法调研
JDK 源码 `Statement.java`；XMLEncoder 序列化基础。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：目标/方法名/参数、execute 返回值与 int 自动装箱。
