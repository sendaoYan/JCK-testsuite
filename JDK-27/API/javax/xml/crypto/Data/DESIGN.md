# javax.xml.crypto.Data — 设计案

## 1. API 功能
javax.xml.crypto.Data 接口。标记接口，无声明成员。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.crypto.Data() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.Data）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DataTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DataTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
