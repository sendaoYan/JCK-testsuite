# javax.xml.catalog.CatalogFeatures.Feature — 设计案

## 1. API 功能
javax.xml.catalog.CatalogFeatures.Feature 类。静态入口：values, valueOf。 实例/声明方法：equalsPropertyName, getPropertyName, defaultValue, getValue, hasSystemProperty。

## 2. Java 用户如何使用
getPropertyName/valueOf/values。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogFeatures.Feature）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 FeatureTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`FeatureTest.testBlackBox()`）覆盖：4 属性名、valueOf、values().length。
