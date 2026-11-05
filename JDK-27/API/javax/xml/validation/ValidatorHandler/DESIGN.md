# javax.xml.validation.ValidatorHandler — 设计案

## 1. API 功能
javax.xml.validation.ValidatorHandler 抽象类。实例/声明方法：setContentHandler, getContentHandler, setErrorHandler, getErrorHandler, setResourceResolver, getResourceResolver, getTypeInfoProvider, getFeature, setFeature, setProperty, getProperty。

## 2. Java 用户如何使用
newValidatorHandler → get/setContentHandler/ErrorHandler/ResourceResolver → getTypeInfoProvider。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.ValidatorHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ValidatorHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ValidatorHandlerTest.testBlackBox()`）覆盖：handler 往返、TypeInfoProvider。

## 5. 深化条目（本批新增，ValidatorHandlerTest 22→40 断言）
- **SAX 管道级联**：namespace-aware XMLReader → ValidatorHandler → 记录 handler，事件序列精确锁定 8 事件；二次 parse 复用。XMLReader 必须 namespaceAware(true) 否则 cvc-elt.1。
- **TypeInfoProvider（PSVI）**：root 匿名类型；name → "string"（XSD ns）；count → "int"（XSD ns）；属性 id → "int"、isSpecified true、isId false。
- **错误通道**：setErrorHandler 后 parse 非法文档不抛、error 恰 1 次含 cvc-complex-type。
- FSP/属性往返；未知 → SAXNotRecognizedException；setContentHandler(null)/setErrorHandler(null) 清除。
