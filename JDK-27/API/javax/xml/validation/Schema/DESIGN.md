# javax.xml.validation.Schema — 设计案

## 1. API 功能
javax.xml.validation.Schema 抽象类。实例/声明方法：newValidator, newValidatorHandler。

## 2. Java 用户如何使用
newSchema → newValidator/newValidatorHandler。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.Schema）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SchemaTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SchemaTest.testBlackBox()`）覆盖：两个 handler 创建。

## 5. 深化条目（本批新增，SchemaTest 11→20 断言）
- **newValidator 多实例独立（核心）**：v1 设记录型 handler → 非法文档不抛、error 恰 1 次；v2 无 handler → 同文档抛 SAXParseException；v2 handler 不受 v1 影响。
- newValidatorHandler 多实例独立（vh1 设 contentHandler 不影响 vh2）。
- 每次调用返回新实例。
