# java.beans.XMLDecoder — 设计案

## 1. API 功能
对象 XML 解码器。构造器 `(InputStream[, owner[, ExceptionListener[, ClassLoader]]])`；readObject/close/get/setOwner/get/setExceptionListener。

## 2. Java 用户如何使用
`XMLDecoder(is).readObject()` 反序列化 XMLEncoder 输出。

## 3. 开源代码实际用法调研
JDK 源码 `XMLDecoder.java`；JavaBeans 长期持久化。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：XML 往返解码 Bean 字段、owner/ExceptionListener 构造器。
