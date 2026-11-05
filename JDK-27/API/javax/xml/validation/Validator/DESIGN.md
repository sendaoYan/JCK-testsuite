# javax.xml.validation.Validator — 设计案

## 1. API 功能
javax.xml.validation.Validator 抽象类。实例/声明方法：reset, validate, setErrorHandler, getErrorHandler, setResourceResolver, getResourceResolver, getFeature, setFeature, setProperty, getProperty。

## 2. Java 用户如何使用
newValidator → validate(有效文档) → validate(无效文档) 抛异常 → reset → 再 validate。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.Validator）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ValidatorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ValidatorTest.testBlackBox()`）覆盖：有效/无效校验、handler 往返、reset。

## 5. 深化条目（本批新增，ValidatorTest 20→54 断言）
- **cvc 消息级断言（核心闭环）**：缺必需子元素 cvc-complex-type.2.4.a（含 "{name}"、行/列 ≥1）；非整数内容 cvc-datatype-valid.1.2.1（引用 'zz'）；未知元素 cvc-complex-type.2.4.d；缺 required 属性 cvc-complex-type.4；顺序颠倒 cvc-complex-type；截断文档 SAXParseException。
- **ErrorHandler 矩阵**：记录型 → 不抛、error 恰 1 次携 cvc-；无 handler 同文档直接抛（对比断言）。
- **Result 形态**：validate(src,null) 仅校验；StreamResult 写出增强输出；DOMSource/SAXSource 通过；可重复使用。
- **reset** 清 ErrorHandler；FSP 往返；未知 feature/property → SAXNotRecognizedException；external-noNamespaceSchemaLocation 往返。
