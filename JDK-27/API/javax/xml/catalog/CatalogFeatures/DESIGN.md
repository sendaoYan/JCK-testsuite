# javax.xml.catalog.CatalogFeatures — 设计案

## 1. API 功能
javax.xml.catalog.CatalogFeatures 类。静态入口：defaults, builder。 实例/声明方法：get。

## 2. Java 用户如何使用
defaults() → get(Feature)；builder().with(Feature,value).build()。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogFeatures）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CatalogFeaturesTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CatalogFeaturesTest.testBlackBox()`）覆盖：默认值、builder 覆盖 + 继承。
