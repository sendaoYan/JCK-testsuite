# javax.xml.catalog.CatalogManager — 设计案

## 1. API 功能
javax.xml.catalog.CatalogManager 类。静态入口：catalog, catalogResolver。

## 2. Java 用户如何使用
catalogResolver(CatalogFeatures) / catalogResolver(CatalogFeatures, URIs)。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogManager）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CatalogManagerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CatalogManagerTest.testBlackBox()`）覆盖：两种 catalogResolver。

## 5. 深化（本批新增）
- **核心闭环（catalog 重写）**：临时 catalog 文件（system/systemSuffix/public/uri/rewriteSystem）→ CatalogManager.catalog(features, file URI) → matchSystem/matchURI/matchPublic 替换结果精确断言（绝对 URI = 临时目录 File URI + 相对条目路径）；finally 删文件断言。
- **系统属性路线**：System.setProperty("javax.xml.catalog.files", 文件 URI) → catalog(defaults) → matchSystem 重写生效；finally clearProperty + 删文件。
- catalogResolver(Catalog, NotFoundAction.CONTINUE/STRICT)：CONTINUE 下 resolveEntity 重写生效；STRICT 下未匹配 → CatalogException。
- 负例：catalog(features, (URI[])null) → NPE；**catalogResolver((Catalog)null) → NPE（消息 JAXP09020006）**；catalog(features, 空 URI[]) → 空 catalog（匹配恒 null）；**缺失 catalog 文件 → 惰性加载不抛、matchSystem 返回 null**。
