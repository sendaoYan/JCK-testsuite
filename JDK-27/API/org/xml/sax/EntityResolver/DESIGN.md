# org.xml.sax.EntityResolver — 设计案

## 1. API 功能
org.xml.sax.EntityResolver 接口。实例/声明方法：resolveEntity。

## 2. Java 用户如何使用
匿名实现 `new org.xml.sax.EntityResolver() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.EntityResolver）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EntityResolverTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EntityResolverTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

### 深化轮次2（行为契约）
黑盒：resolver 返回的替代 InputSource 内容流进 characters（真闭环）；返回 null → 默认解析（缺文件 FileNotFoundException）；setEntityResolver/getEntityResolver 往返；DefaultHandler 默认返回 null。
