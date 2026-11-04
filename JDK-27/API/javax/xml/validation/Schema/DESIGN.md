# javax.xml.validation.Schema — 设计案

## 1. API 功能
javax.xml.validation.Schema 抽象类。实例/声明方法：newValidator, newValidatorHandler。

## 2. Java 用户如何使用
newSchema → newValidator/newValidatorHandler。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.Schema）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SchemaTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SchemaTest.testBlackBox()`）覆盖：两个 handler 创建。
