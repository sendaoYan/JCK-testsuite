# javax.xml.transform.Templates — 设计案

## 1. API 功能
javax.xml.transform.Templates 接口。实例/声明方法：newTransformer, getOutputProperties。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.Templates() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.Templates）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TemplatesTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TemplatesTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- 复用隔离：newTransformer() 两次得不同实例，参数 AA/BB 独立输出精确，默认参 `<e p="">3</e>`；同一 transformer 重复 transform 确定性。
- getOutputProperties() 快照：xsl:output(method=text/indent=yes) 可见；篡改返回副本不影响后续 newTransformer 的属性。
