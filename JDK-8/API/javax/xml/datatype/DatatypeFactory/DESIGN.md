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
- 常量：DATATYPEFACTORY_PROPERTY=="javax.xml.datatype.DatatypeFactory"；DATATYPEFACTORY_IMPLEMENTATION_CLASS=="com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl"。
- 发现机制负例（finally 清属性）：系统属性指向不存在类 → DatatypeConfigurationException（消息 "Provider xxx not found"，cause ClassNotFoundException）；清属性后 newInstance() 恢复；newInstance(未知类, loader) → DCE + cause CNFE。
- newDuration 家族全重载：六 int、BigInteger×5+BigDecimal、dayTime/yearMonth 的 int/BigInteger×4·×2/long/词法各形态（字段与词法精确，如 dayTime(90061000)=="P1DT1H1M1.000S"）；dayTime("P1Y")/yearMonth("P1D") → IAE。
- newXMLGregorianCalendar 家族：int×8（tz 越界 100000 → IAE 消息含 "100,000"）、BigInteger 构造、GregorianCalendar（GMT+08:00 精确映射）、Date/Time 四形态。
- 类形态：public abstract、唯一构造 protected。
- DCE 四构造链（String)/(String,Throwable)/(Throwable)/(无参) 精确 message/cause。
