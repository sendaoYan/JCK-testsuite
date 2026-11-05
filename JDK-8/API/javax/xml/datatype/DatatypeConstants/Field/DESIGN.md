# DatatypeConstants.Field — 设计案

## 1. API 功能
Duration/日历字段描述符单例（final 类）。公开面仅 getId() 与 toString()；六常量 YEARS..SECONDS。

## 2. Java 用户如何使用
Duration.getField(DatatypeConstants.YEARS)/isSet(YEARS) 等作为字段键。

## 3. 开源代码实际用法调研
JDK 源码 DatatypeConstants 自用；JDK test/jdk/javax/xml/datatype 以 Field 常量驱动字段断言。

## 4. 测试用例设计 + 自动化实现
白盒：final、superclass Object、**仅有 getId/toString 两个自有公开方法**。黑盒：六常量 instanceof Field、getId 顺序 0..5（YEARS=0..SECONDS=5）、toString 精确串、两两 identity 互异。
