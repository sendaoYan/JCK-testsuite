# javax.xml.crypto.dsig.CanonicalizationMethod — 设计案

## 1. API 功能
javax.xml.crypto.dsig.CanonicalizationMethod 接口。常量：INCLUSIVE, INCLUSIVE_WITH_COMMENTS, EXCLUSIVE, EXCLUSIVE_WITH_COMMENTS, INCLUSIVE_11, INCLUSIVE_11_WITH_COMMENTS（共 6 个）。 实例/声明方法：getParameterSpec。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.crypto.dsig.CanonicalizationMethod() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.CanonicalizationMethod）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CanonicalizationMethodTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CanonicalizationMethodTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
