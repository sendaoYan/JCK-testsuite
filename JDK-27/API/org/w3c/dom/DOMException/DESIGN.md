# DOMException — 设计案

## 1. API 功能
DOM 异常，RuntimeException 子类。public short code 字段；(short, String) 构造；17 个错误码常量。

## 2. Java 用户如何使用
new DOMException(DOMException.NAMESPACE_ERR, "msg") 读 code/getMessage。

## 3. 开源代码实际用法调研
JDK test/jdk/org/w3c/dom 与 JCK 表驱动校验错误码常量与构造。

## 4. 测试用例设计 + 自动化实现
黑盒：继承链；17 个错误码精确值；构造 code/message/无 cause；null message；code 字段可写。
