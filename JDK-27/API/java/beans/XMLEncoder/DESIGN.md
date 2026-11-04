# java.beans.XMLEncoder — 设计案

## 1. API 功能
对象 XML 编码器（extends Encoder）。构造器 `(OutputStream[, charset[, declaration[, ExceptionListener]]])`；writeObject/flush/close。

## 2. Java 用户如何使用
`XMLEncoder(baos)` + writeObject + close 将 Bean 序列化为 XML；与 XMLDecoder 配对做持久化。

## 3. 开源代码实际用法调研
JDK 源码 `XMLEncoder.java`；JavaBeans 长期持久化格式。

## 4. 测试用例设计 + 自动化实现
白盒：extends Encoder。黑盒（`testBlackBox`）：Bean 编码后 XML 包含属性值。
