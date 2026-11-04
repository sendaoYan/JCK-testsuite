# java.beans.Encoder — 设计案

## 1. API 功能
对象编码器抽象基类（protected 构造器）。get/setOwner、get/setExceptionListener、get/setPersistenceDelegate、writeObject/get/remove。

## 2. Java 用户如何使用
XMLEncoder 继承 Encoder 输出 XML；自定义委托通过 setPersistenceDelegate 注册。

## 3. 开源代码实际用法调研
JDK 源码 `Encoder.java`；XMLEncoder 基类。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：owner/exceptionListener/persistenceDelegate 的 set/get。
