# javax.xml.catalog.CatalogResolver — 设计案

## 1. API 功能
javax.xml.catalog.CatalogResolver 接口。实例/声明方法：resolveEntity, resolve, resolveResource。

## 2. Java 用户如何使用
catalogResolver → instanceof 四接口 → resolveEntity/resolve 未命中返回 null。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogResolver）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CatalogResolverTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CatalogResolverTest.testBlackBox()`）覆盖：接口实现、未命中 null。

## 5. 深化（本批新增）
- **resolveEntity(publicId, systemId) 重写闭环**：匹配 systemId → InputSource.systemId 为替换后绝对 URI；未匹配+RESOLVE=continue → **null**（SAX 契约放行）；resolve(href, base) 匹配 → Source 替换 URI、未匹配+continue → Source 携带原 href。
- RESOLVE 三态：strict 未匹配 → **CatalogException 消息 JAXP09040001 + 原 systemId**；ignore 未匹配 → **非 null 空 InputSource**；continue → null。
- **PREFER 语义闭环**：仅 public 条目 + systemId 未匹配 —— PREFER=public 回退 publicId 命中；PREFER=system → null；**双条目同时命中时 system 条目恒胜（无论 PREFER）**。
- LSResourceResolver 形态 resolveResource(type,ns,pub,sys,base) 按 systemId 重写；4 参 resolveEntity（SAX2 扩展默认方法）未匹配 → null。
