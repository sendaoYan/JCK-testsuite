# XMLGregorianCalendar — 设计案

## 1. API 功能
XML Schema 日期时间抽象类，实现 Cloneable。字段 get/set（year/month/day/timezone/hour/minute/second/millisecond/fractionalSecond/eon）；compare/normalize；equals/hashCode；toXMLFormat/toString；isValid；add(Duration)；toGregorianCalendar；getTimeZone；clone。

## 2. Java 用户如何使用
newXMLGregorianCalendar(lexical) 读字段；set* 构建；compare 比较；add(Duration) 推进。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/datatype 大量表驱动校验字段、时区、比较、格式化。

## 4. 测试用例设计 + 自动化实现
黑盒：+08:00/Z/-05:00/无时区解析与字段/时区分钟数；getMillisecond/getFractionalSecond；字段 setter 与 setMonth(13)/setDay(0)/setHour(-1)→IAE；clear/reset→FIELD_UNDEFINED；compare LESSER/EQUAL/GREATER；equals/hashCode；normalize 进位；add(Duration)；toGregorianCalendar 时区保留与显式时区重载；getTimeZone；clone 独立。

## 5. 深化（本批新增）
- 全字段解析：2024-06-15T10:30:00.500+08:00 → 秒=0/分数=0.5/毫秒=500/tz=480（ fractionalSecond BigDecimal 0.500 compareTo 0.5）。
- toXMLFormat 全等矩阵 14 形态（date/dateTime/分数 .5 保留原样/+08:00/Z/-05:00/纯时间/gYearMonth/gMonthDay/gDay/gYear/2024Z/+14:00→840）；**T24:00:00 归一化为次日 00:00:00 且与次日 00:00 compare==0 且 equals==true**；**+08:60 解析归一为 +09:00（540 分）**；**gMonth 词法 '--06--' 双侧均 IAE（实现不支持，锁定）**。
- 非法词法精确消息：month13 → "Invalid value 13 for Month field."；day32/hour25/minute61 同型；**tz +99:00 → "Invalid value 5,940 for Timezone field."（千分位逗号）**；2024-02-30 → '"..." is not a valid representation of an XML Gregorian Calendar value.'；非零前导格式（2024-6-5/garbage）→ 消息即原文。
- compare：**同一时刻不同表示（+08:00 vs Z）compare==0 且 equals==true 且 hashCode 相等（本实现 equals 跟随 compare）**；字段不定（gYear vs date、gYearMonth vs date、无 tz vs Z）→ INDETERMINATE；compare(null)/add(null) → NPE。
- add 月末钳制四例：2023-01-31+P1M→02-28（非闰）、2024-01-31+P1M→02-29（闰）、-P1M→2023-12-31、2024-03-31+P1D→04-01、date+PT25H→次日。
- normalize：+08:00→02:30:00Z；-12:00→22:30:00Z；无 tz 原样。
- isValid：Feb30 false（仍可 toXMLFormat）；Feb29 闰 true/非闰 false；**year 0 invalid（XSD 1.0）但词法 "0000-01-01"**；负年 -500 valid（"-0500-01-01"、getEonAndYear=-500）；**全字段 FIELD_UNDEFINED isValid==true 但 toXMLFormat/getXMLSchemaType → ISE**。
- 分数互操作：setMillisecond(250)→getFractionalSecond 0.25；setFractionalSecond(0.123456)→getMillisecond 123（截断）；setFractionalSecond(null)→ms UNDEFINED。
- 工厂族：8 参 int（ms=0 词法含 .000）、BigInteger/BigDecimal 构造、GregorianCalendar（固定 GMT+08:00 映射精确）、Date/Time 四形态（**time 5 参第 4 个 int 是毫秒**："10:30:00.005Z"）。
- toGregorianCalendar：tz 480 偏移保留；显式 GMT 得精确 epoch 1718447400000；**getTimeZone(int)：自身 tz 已定义时参数被忽略（恒返回自身 +08:00），未定义时按参数构造**。
