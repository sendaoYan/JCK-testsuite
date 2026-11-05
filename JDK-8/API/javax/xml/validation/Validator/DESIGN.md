# javax.xml.validation.Validator — 设计案

## 1. API 功能
javax.xml.validation.Validator 抽象类：validate(Source)/validate(Source,Result)、reset、get/setErrorHandler、get/setResourceResolver、get/setFeature、get/setProperty。非线程安全。

## 2. Java 用户如何使用
schema.newValidator() → validate(StreamSource)；设 ErrorHandler 收集错误而非抛出；validate(Source, StreamResult) 同时写增强输出。

## 3. 参考来源
Oracle javadoc javax.xml.validation.Validator（ErrorHandler 消费错误语义、reset 重置到创建时状态）；XML Schema Part 1 cvc- 错误码。

## 4. 测试用例设计 + 易错细节（ValidatorTest）
- **cvc 消息级断言（核心闭环）**：固定 XSD（name 必需 xs:string、count 必需 xs:int、id required xs:int）：
  - 合法文档 → 无异常；
  - 缺必需子元素 → SAXParseException 含 "cvc-complex-type.2.4.a" 且消息含 "{name}"，行/列号 ≥1；
  - count 给非整数 → 含 "cvc-datatype-valid.1.2.1" 且引用违规值 'zz'（注意：消息说 "not a valid value for 'integer'"，即 xs:int 归类为 integer）；
  - 未知元素（多子元素）→ 含 "cvc-complex-type.2.4.d"；
  - 缺 required 属性 → 含 "cvc-complex-type.4"；
  - 顺序颠倒 → 含 "cvc-complex-type"；
  - 截断文档（well-formed 错误）→ SAXParseException。
- **ErrorHandler 矩阵**：记录型 handler → validate 不抛、error 恰好 1 次、回调消息携带 cvc-；不设 handler 同一文档直接抛（对比断言）；handler/resolver set-get-null 往返。
- **Result 形态**：validate(src, null) 仅校验；validate(src, StreamResult) 输出写出（含 <root 与 <count>42</count>）；DOMSource/SAXSource 输入形态均通过；同一 Validator 可重复使用。
- **reset**：清除 ErrorHandler（get 回 null），之后可继续校验。
- **Feature/Property**：FEATURE_SECURE_PROCESSING true/false 往返；未知 feature/property → SAXNotRecognizedException；external-noNamespaceSchemaLocation 属性 set/get 往返。
