# javax.xml.catalog.CatalogResolver.NotFoundAction — 设计案

## 1. API 功能
javax.xml.catalog.CatalogResolver.NotFoundAction 类。静态入口：values, valueOf, getType。

## 2. Java 用户如何使用
valueOf/getType/values。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogResolver.NotFoundAction）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NotFoundActionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NotFoundActionTest.testBlackBox()`）覆盖：valueOf、getType、values().length。
