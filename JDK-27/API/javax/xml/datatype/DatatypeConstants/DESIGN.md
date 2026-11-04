# DatatypeConstants — 设计案

## 1. API 功能
XML Schema 数据类型常量。月份 1..12；比较 LESSER/EQUAL/GREATER/INDETERMINATE；FIELD_UNDEFINED；Field 单例 YEARS..SECONDS；schema QName 常量；MAX/MIN_TIMEZONE_OFFSET。

## 2. Java 用户如何使用
DatatypeConstants.JANUARY 等作字面量；compare 返回与 DatatypeConstants.LESSER 比较。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/datatype 引用这些常量校验值。

## 4. 测试用例设计 + 自动化实现
黑盒：12 个月份值；比较常量 -1/0/1/2；FIELD_UNDEFINED==Integer.MIN_VALUE；schema QName 精确值（DATETIME/…/DURATION_DAYTIME=dayTimeDuration 等）；时区上下限 ±840；Field 单例身份与 toString。
