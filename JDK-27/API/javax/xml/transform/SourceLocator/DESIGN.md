# javax.xml.transform.SourceLocator — 设计案

## 1. API 功能
javax.xml.transform.SourceLocator 接口。实例/声明方法：getPublicId, getSystemId, getLineNumber, getColumnNumber。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.SourceLocator() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.SourceLocator）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SourceLocatorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SourceLocatorTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- 方法集恰 4 个（反射精确比对）。
- TransformerException(String, locator) 携带：getLocator() 同引用、位置串 `"; SystemID: …; Line#: …; Column#: …"` 逐字符锁定。
