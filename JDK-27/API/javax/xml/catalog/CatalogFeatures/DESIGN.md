# javax.xml.catalog.CatalogFeatures — 设计案

## 1. API 功能
javax.xml.catalog.CatalogFeatures 类。静态入口：defaults, builder。 实例/声明方法：get。

## 2. Java 用户如何使用
defaults() → get(Feature)；builder().with(Feature,value).build()。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.catalog.CatalogFeatures）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CatalogFeaturesTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CatalogFeaturesTest.testBlackBox()`）覆盖：默认值、builder 覆盖 + 继承。

## 5. 深化（本批新增）
- defaults() 矩阵：**FILES==null（未设）**、PREFER=="public"、DEFER=="true"、RESOLVE=="strict"；与 Feature.defaultValue() 逐项一致。
- builder 语义：FILES 绝对 URI 往返；**重复 with() 后值覆盖（last-wins）**；未设特征继承默认。
- 校验（with() 即抛，非 build()）：**with(null, v) → NPE**；PREFER/DEFER/RESOLVE 非法值 → IAE 消息含 **JAXP09020005**、值与特征名；FILES 相对 URI → IAE "not absolute"；FILES "  " → IAE（URI 语法）。
- get(null) → NPE。
- **无值语义（锁定）**：builder().build() 两次 equals==false；defaults() != builder().build()（equals false）。
