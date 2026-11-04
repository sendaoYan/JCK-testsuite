# javax.xml.datatype.XMLGregorianCalendar — 设计案

## 1. API 功能
javax.xml.datatype.XMLGregorianCalendar 抽象类。实例/声明方法：clear, reset, setYear, setMonth, setDay, setTimezone, setTime, setHour, setMinute, setSecond, setMillisecond, setFractionalSecond, getEon, getYear, getEonAndYear, getMonth, getDay, getTimezone, getHour, getMinute, getSecond, getMillisecond, getFractionalSecond, compare, normalize, equals, hashCode, toXMLFormat, getXMLSchemaType, toString, isValid, add, toGregorianCalendar, getTimeZone, clone。

## 2. Java 用户如何使用
newXMLGregorianCalendar(String) → getters 精确 → compare/clone/toGregorianCalendar/normalize → toXMLFormat 往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.datatype.XMLGregorianCalendar）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLGregorianCalendarTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLGregorianCalendarTest.testBlackBox()`）覆盖：字段、分数秒、时区、比较、克隆、类型、setDay/reset、normalize。
