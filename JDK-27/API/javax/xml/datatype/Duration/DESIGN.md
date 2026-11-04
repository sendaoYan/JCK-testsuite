# Duration — 设计案

## 1. API 功能
XML Schema 时长抽象类。getSign/getYears/Months/Days/Hours/Minutes/Seconds；getField/isSet；add/subtract/multiply/negate；compare/isLongerThan/isShorterThan；equals/hashCode/toString；getTimeInMillis；addTo(Calendar/Date)；getXMLSchemaType；normalizeWith。

## 2. Java 用户如何使用
newDuration("P1Y2M3DT4H5M6S") 读字段；add/subtract/multiply/negate 运算；compare 比较。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/datatype 校验 Duration 算术、比较与非法 lexical。

## 4. 测试用例设计 + 自动化实现
黑盒：六字段与 sign；getField 已设返回/未设 null、isSet；负数 duration sign=-1；add/subtract/multiply/negate；compare/isLongerThan/isShorterThan；equals/hashCode/toString；getXMLSchemaType（DURATION/DAYTIME/YEARMONTH）；addTo(Calendar)/addTo(Date) 推进。
