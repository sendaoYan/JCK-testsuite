# javax.xml.validation.ValidatorHandler — 设计案

## 1. API 功能
javax.xml.validation.ValidatorHandler（实现 org.xml.sax.ContentHandler）：流式校验 SAX 管道中间件。setContentHandler/getContentHandler、set/getErrorHandler、set/getResourceResolver、getTypeInfoProvider、get/setFeature、get/setProperty。

## 2. Java 用户如何使用
XMLReader xr → xr.setContentHandler(validatorHandler) → validatorHandler.setContentHandler(下游 DefaultHandler) → xr.parse(...)：上游事件先被校验/增强再转发下游。

## 3. 参考来源
Oracle javadoc javax.xml.validation.ValidatorHandler（管道语义、TypeInfoProvider 仅在回调内有效）；W3C XML Schema PSVI；OpenJDK 实测。

## 4. 测试用例设计 + 易错细节（ValidatorHandlerTest）
- **SAX 管道级联（核心）**：namespace-aware XMLReader → ValidatorHandler → 记录型 handler，事件序列精确锁定 startDocument,start:root,start:name,end:name,start:count,end:count,end:root,endDocument；同一 handler 二次 parse 复用。
  - 易错：XMLReader 必须 namespaceAware(true)（SAXParserFactory 默认 false），否则校验不到声明、报 cvc-elt.1。
- **TypeInfoProvider（PSVI 增强）**：回调内 getElementTypeInfo()：root 为匿名复杂类型（名字非 null、namespace null/空）；name 元素 → "string" + XSD ns；count 元素 → "int" + XSD ns；属性 id → "int"、isSpecified(0)=true、isIdAttribute(0)=false。
- **错误通道**：vh.setErrorHandler(记录型) 后 parse 非法文档 → 不抛、error 恰好 1 次含 "cvc-complex-type"。
- **配置矩阵**：FEATURE_SECURE_PROCESSING 往返；未知 feature/property → SAXNotRecognizedException；external-noNamespaceSchemaLocation set/get 往返；setContentHandler(null)/setErrorHandler(null) 清除。
