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
- 同 JDK-8 侧全部条目（词法矩阵/毫秒工厂/月日分离加法/round-trip/getXMLSchemaType 完整性/月末钳制）。
- **JDK-8 vs JDK-27 锁定差异（Duration.compare 语义修正）**：P1M vs P28D —— JDK-8 compare==EQUAL 且 equals==true（但 hashCode 不同，equals/hashCode 契约破洞）；JDK-27 compare==INDETERMINATE 且 equals==false。P1Y vs P365D 同样（8=EQUAL / 27=INDETERMINATE）。测试按 jdkFeature()<=8 分支各自锁定。
- P1M vs P29D/P30D/P31D 双侧均 INDETERMINATE；P1M vs P32D 双侧 LESSER（这些不受版本影响）。
