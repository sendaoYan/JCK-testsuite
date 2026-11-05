# DatatypeConfigurationException — 设计案

## 1. API 功能
javax.xml.datatype.DatatypeConfigurationException：工厂查找/构造失败的受检异常。构造 ()/(String)/(String,Throwable)/(Throwable)；extends java.lang.Exception。

## 2. Java 用户如何使用
捕获 DatatypeFactory.newInstance() 的失败；或 (String,Throwable) 包装底层原因。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/datatype 异常链用例；本仓库经 DatatypeFactory 系统属性负例真实触发（消息 "Provider ... not found"，cause ClassNotFoundException）。

## 4. 测试用例设计 + 自动化实现
黑盒：四构造链 message/cause 精确（(Throwable) 的 message 取 cause.toString）；Serializable（经 Exception）；白盒：4 构造器清单（String/(String,Throwable)/(Throwable)/() 各一）、superclass Exception、具体类非抽象。
