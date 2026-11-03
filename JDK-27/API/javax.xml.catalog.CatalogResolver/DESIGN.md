# javax.xml.catalog.CatalogResolver — 设计案

## 1. API 功能
javax.xml.catalog.CatalogResolver 接口。实例/声明方法：resolveEntity, resolve, resolveResource。

## 2. Java 用户如何使用
catalogResolver → instanceof 四接口 → resolveEntity/resolve 未命中返回 null。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogResolver）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CatalogResolverTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CatalogResolverTest.testBlackBox()`）覆盖：接口实现、未命中 null。
