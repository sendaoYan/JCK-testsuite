# javax.xml.catalog.CatalogFeatures.Builder — 设计案

## 1. API 功能
javax.xml.catalog.CatalogFeatures.Builder 类。实例/声明方法：with, build。

## 2. Java 用户如何使用
builder() → with → build。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogFeatures.Builder）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 BuilderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`BuilderTest.testBlackBox()`）覆盖：with 返回 this、build 生效。
