# javax.xml.datatype.Duration — 设计案

## 1. API 功能
javax.xml.datatype.Duration 抽象类。实例/声明方法：getXMLSchemaType, getSign, getYears, getMonths, getDays, getHours, getMinutes, getSeconds, getTimeInMillis, getField, isSet, add, addTo, subtract, multiply, negate, normalizeWith, compare, isLongerThan, isShorterThan, equals, hashCode, toString。

## 2. Java 用户如何使用
newDuration → getField/isSet → add/subtract/multiply/negate → compare/isLongerThan/equals。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.datatype.Duration）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DurationTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DurationTest.testBlackBox()`）覆盖：字段、符号、四种运算、比较、equals/hashCode、类型。
