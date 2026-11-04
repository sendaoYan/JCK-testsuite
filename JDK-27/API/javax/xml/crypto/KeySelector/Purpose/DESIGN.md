# javax.xml.crypto.KeySelector.Purpose — 设计案

## 1. API 功能
javax.xml.crypto.KeySelector.Purpose 类。实例/声明方法：toString。

## 2. Java 用户如何使用
常量 toString() 精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.KeySelector.Purpose）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 PurposeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`PurposeTest.testBlackBox()`）覆盖：四个用途常量的字符串表示。。
