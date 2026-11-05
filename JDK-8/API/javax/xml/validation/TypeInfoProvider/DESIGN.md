# javax.xml.validation.TypeInfoProvider — 设计案

## 1. API 功能
javax.xml.validation.TypeInfoProvider 抽象类（protected 构造）：getElementTypeInfo()、getAttributeTypeInfo(int)、isIdAttribute(int)、isSpecified(int)。仅在 ValidatorHandler 的 startElement 回调内有效。

## 2. Java 用户如何使用
validatorHandler.getTypeInfoProvider() 保存引用 → 在 setContentHandler 的 startElement 内查询当前元素/属性的类型信息（PSVI）。

## 3. 参考来源
Oracle javadoc javax.xml.validation.TypeInfoProvider 与 org.w3c.dom.TypeInfo；W3C XML Schema Part 1（类型系统）。

## 4. 测试用例设计 + 易错细节（TypeInfoProviderTest）
- **真实实例闭环**：固定 XSD（name=xs:string、count=xs:int、id required xs:int）经 ValidatorHandler 管道：root 匿名类型名非 null 且 namespace null/空；name → "string"/XSD ns；count → "int"/XSD ns（类型名不带 xs: 前缀，是 localPart）。
- **子类派发**：匿名子类实现四方法各自返回固定值断言派发（抽象契约）。
- protected 构造：getConstructors().length == 0。
- 易错：provider 只在回调内查询有效；XMLReader 需 namespace-aware。
