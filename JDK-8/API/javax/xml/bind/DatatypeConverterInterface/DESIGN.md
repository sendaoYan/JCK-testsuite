# javax.xml.bind.DatatypeConverterInterface 契约覆盖设计（JDK-8）

## 1. API 功能
XML Schema 数据类型与 Java 词法互转的 38 方法接口（19 个 parse*：词法→Java 值；19 个 print*：Java 值→词法）。JAXB 提供方（运行时）用它把内置 xsd 类型转换外包给可插拔实现；`DatatypeConverter.setDatatypeConverter` 注册后，`DatatypeConverter` 全部静态方法委托到注册实例。

## 2. 使用方式
```java
class MyConverter implements DatatypeConverterInterface { ...38 个方法... }
DatatypeConverter.setDatatypeConverter(new MyConverter()); // 必须在第一次静态 parse/print 之前
DatatypeConverter.parseInt("42");   // 此后经 MyConverter 路由
```
注册窗口契约（实测锁定）：第一次使用静态方法前注册才生效；**此后再注册被静默忽略（不抛）**；`setDatatypeConverter(null)` 抛 IllegalArgumentException。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/DatatypeConverterInterface.html
- JSR 222 (JAXB 2.2) 规范附录（DatatypeConverter）
- OpenJDK 8u RI 源码 `javax/xml/bind/DatatypeConverterInterface.java`、`DatatypeConverter.java`
- 本仓库测试：`DatatypeConverterInterfaceTest.java`

## 4. 测试用例设计 + 易错规范细节
- 表面精确锁定：恰好 38 个声明方法（19 parse + 19 print）、无常量、无父接口、全 public abstract；8 个原生类型返回签名逐一对号（parseInt→int、parseBoolean→boolean、parseByte→byte、parseShort→short、parseFloat→float、printInt(int)→String 等）。
- 自造 Marker 实现全部 38 方法并逐个调用断言返回值（编译通过即证明表面完整）。
- 注册通道（真实闭环）：**先注册后使用** → `DatatypeConverter.parseInt("42")` 返回 Marker 值 111、`printInt` 返回 "P-in"、`parseString` 返回 "M-s"，且 Marker 日志恰记录 3 次；第二次注册第二个 Marker 被忽略（parseInt 仍 111、第二实例日志 0）；null 注册抛 IAE 后路由不变。
- 易错点：
  - Marker 的 parse 方法**绝不能委托回 DatatypeConverter 静态方法**（无限递归），必须返回常量；
  - 测试内任何依赖真实转换的断言必须放在注册之前；
  - `setDatatypeConverter` 涉及 JAXBPermission("setDatatypeConverter")，但仅在 SecurityManager 存在时检查——本测试不装 SM。
