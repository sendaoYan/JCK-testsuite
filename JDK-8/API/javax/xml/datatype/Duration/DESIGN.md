# Duration — 设计案

## 1. API 功能
XML Schema 时长抽象类。getSign/getYears/Months/Days/Hours/Minutes/Seconds；getField/isSet；add/subtract/multiply/negate；compare/isLongerThan/isShorterThan；equals/hashCode/toString；getTimeInMillis；addTo(Calendar/Date)；getXMLSchemaType；normalizeWith。

## 2. Java 用户如何使用
newDuration("P1Y2M3DT4H5M6S") 读字段；add/subtract/multiply/negate 运算；compare 比较。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/datatype 校验 Duration 算术、比较与非法 lexical。

## 4. 测试用例设计 + 自动化实现
黑盒：六字段与 sign；getField 已设返回/未设 null、isSet；负数 duration sign=-1；add/subtract/multiply/negate；compare/isLongerThan/isShorterThan；equals/hashCode/toString；getXMLSchemaType（DURATION/DAYTIME/YEARMONTH）；addTo(Calendar)/addTo(Date) 推进。

## 5. 深化（本批新增，ISO 8601 主线）
- 词法矩阵：PT0.5S（getSeconds==0、getField(SECONDS)=BigDecimal 0.5）；PT0S（sign==0）；-P1D；**P1W 两侧均 IAE（周不支持）**；P/PX/P1X/PT/P1D1Y/P1DT/pt1h → IAE（消息即原文）；**P0.5Y → NumberFormatException**（实现怪癖，非秒字段小数走 BigDecimal 解析）；null → NPE。
- newDuration(long)：86400000→P0Y0M1DT0H0M0.000S（**词法保留 .000 分数**）；180000→3 分；-3600000→sign -1。
- add/subtract：**P1M25D+P1M10D=P2M35D（月日分离不进位，锁定）**；P1M25D-P1M10D=P0M15D；**P2M5D-P1M10D → IllegalStateException（逐字段下溢致符号不定）**。
- compare：P1D vs PT24H / P1Y vs P12M / PT1H vs PT60M → EQUAL；**本实现 equals/hashCode 跟随 compare（P1D equals PT24H==true）**；P1M vs P30D/P29D → INDETERMINATE（返回 2 不抛异常）；P1M vs P32D → LESSER；compare(null)→NPE。
- getTimeInMillis：纯 dayTime 精确毫秒；**含月/年不抛 ISE**，以锚点日期实际推算（P1M 自 epoch=31 天、P1Y=365 天）。
- toString→newDuration round-trip（8 种形态全 equals）；getXMLSchemaType 要求字段组完整：P1M/P1D/PT1H/P1Y1D 单独 → ISE。
- addTo(Date) 月末钳制：2023-01-31+P1M→02-28；2024-01-31+P1M→02-29（闰年）。
- 负例表：newDuration("P")/("P1W") → IAE；newDuration("P0.5Y") → NumberFormatException；newDuration(null) → NPE；subtract 字段下溢 → ISE；compare(null) → NPE。
