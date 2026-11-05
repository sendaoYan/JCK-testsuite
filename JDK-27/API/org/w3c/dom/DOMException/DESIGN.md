# DOMException — 设计案

## 1. API 功能
DOM 异常，RuntimeException 子类。public short code 字段；(short, String) 构造；17 个错误码常量。

## 2. Java 用户如何使用
new DOMException(DOMException.NAMESPACE_ERR, "msg") 读 code/getMessage。

## 3. 开源代码实际用法调研
JDK test/jdk/org/w3c/dom 与 JCK 表驱动校验错误码常量与构造。

## 4. 测试用例设计 + 自动化实现
黑盒：继承链；17 个错误码精确值；构造 code/message/无 cause；null message；code 字段可写。

## 深化第 2 轮（契约级，2026-11-05）
真实触发矩阵（一码一例）：substringData(9,1)→1、第二文档根→3、跨文档 append→4、createElement('a b')→5、Entity.appendChild→7、removeNamedItem(missing)→8、setXmlVersion(9.9)→9、detach 后 NodeIterator.nextNode→11、createElementNS(null,'p:x')→14；构造器 code/message 存取、null message 保留、initCause 可用、code 公有可变、TYPE_MISMATCH_ERR=17。消息只断言非空（JDK-8 尾部空格差异）。
