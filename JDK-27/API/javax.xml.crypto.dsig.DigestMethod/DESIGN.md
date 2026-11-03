# javax.xml.crypto.dsig.DigestMethod — 设计案

## 1. API 功能
javax.xml.crypto.dsig.DigestMethod 接口。常量：SHA1, SHA224, SHA256, SHA384, SHA512, RIPEMD160, SHA3_224, SHA3_256, SHA3_384, SHA3_512（共 10 个）。 实例/声明方法：getParameterSpec。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.crypto.dsig.DigestMethod() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.DigestMethod）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DigestMethodTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DigestMethodTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
