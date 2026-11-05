# javax.xml.catalog.CatalogFeatures.Builder — 设计案

## 1. API 功能
javax.xml.catalog.CatalogFeatures.Builder 类。实例/声明方法：with, build。

## 2. Java 用户如何使用
builder() → with → build。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogFeatures.Builder）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 BuilderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`BuilderTest.testBlackBox()`）覆盖：with 返回 this、build 生效。

## 5. 深化（本批新增）
- 链式 with() 返回 this；一次链设置四特征全部往返取回。
- **builder 可复用**：先 build() 再 with() 再 build() —— 第一次结果保持旧值、第二次见新值。
- with() 即时校验（IAE/NPE 在 build() 之前抛出）。
- 空 build() 得到文档默认值（FILES null/PREFER public/DEFER true/RESOLVE strict）。
