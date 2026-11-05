# Locator2Impl — 设计案

## 1. API 功能
Locator2 可变实现，继承 LocatorImpl：setXMLVersion/setEncoding + 拷贝构造。

## 2. Java 用户如何使用
手工构造带版本/编码的定位器（测试夹具、桥接）。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：新对象 version/encoding null、位置 0/0（JDK 实现默认，非文档的 -1）；两 setter 往返；继承 id/位置 setter；拷贝构造快照含版本编码；extends LocatorImpl 锁定。
