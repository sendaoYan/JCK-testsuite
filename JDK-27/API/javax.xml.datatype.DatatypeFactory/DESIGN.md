# javax.xml.datatype.DatatypeFactory — 设计案

## 1. API 功能
javax.xml.datatype.DatatypeFactory 抽象类。常量：DATATYPEFACTORY_PROPERTY（共 1 个）。 静态入口：newDefaultInstance, newInstance。 实例/声明方法：newDuration, newDurationDayTime, newDurationYearMonth, newXMLGregorianCalendar, newXMLGregorianCalendarDate, newXMLGregorianCalendarTime。

## 2. Java 用户如何使用
newInstance → newDuration(String/YearMonth/DayTime) → getField 精确；newXMLGregorianCalendar(String/int) → getters/toXMLFormat 往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.datatype.DatatypeFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DatatypeFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DatatypeFactoryTest.testBlackBox()`）覆盖：三种 duration 变体 + 负号、三种 calendar 构造、类型 QName、newDefaultInstance。
