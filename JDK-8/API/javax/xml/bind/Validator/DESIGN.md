# Validator 设计案（JDK-8 契约测试）

## 1. API 功能
JAXB 1.0 校验接口（validate/validateRoot/setEventHandler/getEventHandler/setProperty/getProperty），2.x 中被 Schema 机制取代。

## 2. 使用方式
JAXB 1.0 时代由 ctx.createValidator() 获取；JAXB 2.x RI 直接拒绝。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/bind/)、JSR 222 (JAXB 2.2) 规范、OpenJDK 8u 内置 RI（com.sun.xml.internal.bind.v2）、本仓库 JAXBContextTest 及同包族测试。

## 4. 测试用例设计 + 易错规范细节
- 接口面：6 方法签名、validate/validateRoot 返回 boolean 且声明 JAXBException。
- **实测锁定**：JAXBContext.createValidator() 抛 UnsupportedOperationException（"This feature is not available in JAXB RI 2.0"）——拿不到实例，行为断言只能走签名反射。
- 接口与 createValidator 方法都**不带运行时 @Deprecated 注解**（javadoc 级弃用，反射测不到）。
