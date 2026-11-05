# org.xml.sax.ext.EntityResolver2 — 设计案

## 1. API 功能
org.xml.sax.ext.EntityResolver2 接口。实例/声明方法：getExternalSubset, resolveEntity。

## 2. Java 用户如何使用
匿名实现 `new org.xml.sax.ext.EntityResolver2() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.ext.EntityResolver2）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EntityResolver2Test 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EntityResolver2Test.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

### 深化轮次2（行为契约）
黑盒：外部 DTD 子集引用走四参 resolveEntity(name=null, systemId)；返回 null → 默认解析（FileNotFoundException）；纯 EntityResolver 两参通道对照。
