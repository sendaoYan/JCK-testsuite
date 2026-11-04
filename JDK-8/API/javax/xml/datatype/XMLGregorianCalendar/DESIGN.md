# XMLGregorianCalendar — 设计案

## 1. API 功能
XML Schema 日期时间抽象类，实现 Cloneable。字段 get/set（year/month/day/timezone/hour/minute/second/millisecond/fractionalSecond/eon）；compare/normalize；equals/hashCode；toXMLFormat/toString；isValid；add(Duration)；toGregorianCalendar；getTimeZone；clone。

## 2. Java 用户如何使用
newXMLGregorianCalendar(lexical) 读字段；set* 构建；compare 比较；add(Duration) 推进。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/datatype 大量表驱动校验字段、时区、比较、格式化。

## 4. 测试用例设计 + 自动化实现
黑盒：+08:00/Z/-05:00/无时区解析与字段/时区分钟数；getMillisecond/getFractionalSecond；字段 setter 与 setMonth(13)/setDay(0)/setHour(-1)→IAE；clear/reset→FIELD_UNDEFINED；compare LESSER/EQUAL/GREATER；equals/hashCode；normalize 进位；add(Duration)；toGregorianCalendar 时区保留与显式时区重载；getTimeZone；clone 独立。
