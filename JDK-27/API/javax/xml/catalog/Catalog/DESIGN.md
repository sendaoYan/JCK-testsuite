# javax.xml.catalog.Catalog — 设计案

## 1. API 功能
javax.xml.catalog.Catalog 接口。实例/声明方法：matchSystem, matchPublic, matchURI, catalogs。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.catalog.Catalog() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.Catalog）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CatalogTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CatalogTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本批新增）
- **catalog 重写真闭环**：临时 OASIS XML catalog（system/systemSuffix/public/uri/rewriteSystem 五类条目）→ CatalogManager.catalog(features, uri) → matchSystem/matchURI/matchPublic 断言**替换后绝对 URI 精确值**（相对 uri 以 catalog 文件位置解析，"file:/tmp/.../replaced/system.dtd" 形态）；finally 删临时目录。
- 匹配优先级：**精确 system 条目胜过 systemSuffix**；systemSuffix 仅在无精确匹配时用；rewriteSystem 前缀替换先于 suffix。
- null 参数分型（锁定）：**matchSystem(null) → NPE；matchPublic(null)/matchURI(null) → null**。
- catalogs()：平 catalog 计数 0；**nextCatalog 子目录计数 1**。
- 损坏 catalog（非 XML）→ CatalogException 消息含 **JAXP09030002**；错误根命名空间 → 可加载但匹配 null。
- 方法面（javap）：只有 matchSystem/matchPublic/matchURI/catalogs，**无 matchNamespace、无 getCatalogManager**（任务简报中的 matchNamespace 不存在，以 javap 为准记录）。
