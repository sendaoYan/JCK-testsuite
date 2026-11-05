# DatatypeFactory — 设计案

## 1. API 功能
XML Schema 数据类型工厂。newInstance()；newDuration(多重重载)/newDurationDayTime/newDurationYearMonth；newXMLGregorianCalendar(多重重载)/newXMLGregorianCalendarDate/Time；常量 DATATYPEFACTORY_PROPERTY/IMPLEMENTATION_CLASS。

## 2. Java 用户如何使用
DatatypeFactory.newInstance() 后 newDuration("P1Y2M3DT4H5M6S")、newXMLGregorianCalendar("2001-10-26T21:32:52+08:00") 读字段。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/datatype 大量表驱动校验 lexical 解析与非法输入。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；newDuration(String) 解析六字段、null→NPE、invalid→IAE；newDuration(long) 毫秒→秒；dayTime/yearMonth 字段约束与 invalid→IAE；BigInteger 重载；newXMLGregorianCalendar 各重载与 null→NPE/invalid→IAE；date/time 便捷工厂与 month13/minute60→IAE。JDK-27 追加 newDefaultInstance。

## 5. 深化（本批新增）
- 与 JDK-8 侧相同条目（常量/发现机制负例+恢复/newDuration 与 newXMLGregorianCalendar 全重载/DCE 链/类形态）。
- JDK-27 独有：newDefaultInstance()（JDK-19+）。
- 双侧差异：无（除 newDefaultInstance 仅 27 存在）。
