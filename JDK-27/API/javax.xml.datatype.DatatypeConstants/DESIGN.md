# javax.xml.datatype.DatatypeConstants — 设计案

## 1. API 功能
javax.xml.datatype.DatatypeConstants 类。常量：JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER, LESSER, EQUAL, GREATER, INDETERMINATE, FIELD_UNDEFINED, MAX_TIMEZONE_OFFSET, MIN_TIMEZONE_OFFSET（共 19 个）。

## 2. Java 用户如何使用
常量精确断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.datatype.DatatypeConstants）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DatatypeConstantsTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DatatypeConstantsTest.testBlackBox()`）覆盖：月份、比较、FIELD_UNDEFINED、时区偏移、Field、5 个 QName。
