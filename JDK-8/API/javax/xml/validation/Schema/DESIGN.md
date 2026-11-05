# javax.xml.validation.Schema — 设计案

## 1. API 功能
javax.xml.validation.Schema 不可变编译期 schema（线程安全）：newValidator() 产出校验器、newValidatorHandler() 产出 SAX 管道校验器。

## 2. Java 用户如何使用
由 SchemaFactory.newSchema(...) 获得；`schema.newValidator().validate(source)` 校验文档；`newValidatorHandler()` 挂入 SAX 管道做流式校验与 PSVI 增强。

## 3. 参考来源
Oracle javadoc javax.xml.validation.Schema（不可变性与实例独立性契约）；XML Schema Part 1；OpenJDK XMLSchema 实测。

## 4. 测试用例设计 + 易错细节（SchemaTest）
- **newValidator 多实例独立（核心）**：同一 Schema 的 v1/v2 是不同对象；v1.setErrorHandler(记录型) 后校验非法文档 → 不抛且 error 回调恰好 1 次；v2 无 handler → 同一文档直接抛 SAXParseException；v2.getErrorHandler() 不受 v1 影响。
- **newValidatorHandler 多实例独立**：vh1.setContentHandler(ch) 后 vh1.getContentHandler()==ch；vh2.getContentHandler() 仍 null。
- 每次调用返回新实例（!= 断言）。
- 易错：Schema 自身无校验方法，所有校验经 Validator/ValidatorHandler；文档固定用 cvc 反例（缺必需子元素）。
