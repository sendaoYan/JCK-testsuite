# DatatypeConverter 设计案（JDK-8 契约测试）

## 1. API 功能
XML Schema 内置类型 parse/print 静态对偶表：数值（int/long/short/byte/float/double/integer/decimal/unsignedInt/unsignedShort）、字符串（string/anySimpleType/boolean）、二进制（base64/hex）、时间（dateTime/date/time）、QName。

## 2. 使用方式
DatatypeConverter.parseXxx(lexical)/printXxx(value) 静态直调；RI 默认自带转换器实现。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 数值对偶表：全部 parse 精确值 + print 精确串（含 MAX/MIN_VALUE 边界、负数）。
- base64/hex 双向：QUJD↔{65,66,67}、00ff 值表、空数组空串、小写 hex 可解析。
- 时间：parseDateTime("2024-01-15T10:30:00Z") 全字段；printDateTime 词法前缀 2024-01-15T；**parseDate/parseTime/printDate/printTime 用 Calendar**（非 java.util.Date——签名实测）。
- 负例：parseInt(garbage/null) → NumberFormatException；parseDateTime(garbage/null) → IAE 或 NPE；parseHexBinary 奇数长度/非 hex → IAE；**parseBase64Binary 对垃圾宽松**（跳过非字母字符不抛——实测）。
- **无 duration 方法**（duration 归 javax.xml.datatype，反射断言不存在）。
