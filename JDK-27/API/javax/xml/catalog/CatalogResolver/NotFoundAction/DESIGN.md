# javax.xml.catalog.CatalogResolver.NotFoundAction — 设计案

## 1. API 功能
javax.xml.catalog.CatalogResolver.NotFoundAction 类。静态入口：values, valueOf, getType。

## 2. Java 用户如何使用
valueOf/getType/values。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogResolver.NotFoundAction）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NotFoundActionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NotFoundActionTest.testBlackBox()`）覆盖：valueOf、getType、values().length。

## 5. 深化（本批新增）
- 枚举面：**恰 3 常量、声明序 CONTINUE(0)/IGNORE(1)/STRICT(2)**；**toString() 被覆写为小写字面量（"continue"/"ignore"/"strict"，≠ name()）**——与 getType 输入互为镜像。
- getType(String)：小写严格匹配（大小写敏感，"STRICT" → IAE）；未知/null → IAE 消息含 JAXP09020005；**getType(null) → IAE（非 NPE）**。
- valueOf 往返；valueOf("bogus") → IAE。
