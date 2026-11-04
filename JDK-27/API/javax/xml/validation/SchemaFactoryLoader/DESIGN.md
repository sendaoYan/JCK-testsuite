# javax.xml.validation.SchemaFactoryLoader — 设计案

## 1. API 功能
javax.xml.validation.SchemaFactoryLoader 抽象类。实例/声明方法：newFactory。

## 2. Java 用户如何使用
子类覆盖 newFactory(lang) → 命中/未命中。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.SchemaFactoryLoader）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SchemaFactoryLoaderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SchemaFactoryLoaderTest.testBlackBox()`）覆盖：命中返回、未知返回 null。
