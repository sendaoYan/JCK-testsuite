# javax.xml.validation.SchemaFactoryLoader — 设计案

## 1. API 功能
javax.xml.validation.SchemaFactoryLoader 抽象类（protected 构造、唯一方法 newFactory(String schemaLanguage)）：schema 语言到 SchemaFactory 的 SPI 扩展点。

## 2. Java 用户如何使用
平台默认不提供 loader（SchemaFactory.newInstance 自行查找）；第三方 schema 语言提供者实现 newFactory 按语言分发。

## 3. 参考来源
Oracle javadoc javax.xml.validation.SchemaFactoryLoader；ServiceLoader 发现机制；OpenJDK 实测（无平台 provider）。

## 4. 测试用例设计 + 易错细节（SchemaFactoryLoaderTest）
- **子类派发（核心）**：匿名实现 newFactory → W3C 语言返回委托工厂、未知语言/null 返回 null。
- **平台实测**：ServiceLoader.load(SchemaFactoryLoader.class) 发现 0 个 provider（锁定实际：JDK 无内置 loader）。
- 结构：abstract、superclass Object、newFactory 方法存在、无公共构造器。
