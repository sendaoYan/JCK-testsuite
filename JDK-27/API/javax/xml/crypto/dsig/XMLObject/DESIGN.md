# javax.xml.crypto.dsig.XMLObject — 设计案

## 1. API 功能
javax.xml.crypto.dsig.XMLObject 接口。常量：TYPE（共 1 个）。 实例/声明方法：getContent, getId, getMimeType, getEncoding。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.crypto.dsig.XMLObject() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.XMLObject）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLObjectTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLObjectTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
