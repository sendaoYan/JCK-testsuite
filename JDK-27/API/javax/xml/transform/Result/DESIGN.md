# javax.xml.transform.Result — 设计案

## 1. API 功能
javax.xml.transform.Result 接口。常量：PI_DISABLE_OUTPUT_ESCAPING, PI_ENABLE_OUTPUT_ESCAPING（共 2 个）。 实例/声明方法：setSystemId, getSystemId。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.Result() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.Result）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ResultTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- PI_DISABLE/ENABLE_OUTPUT_ESCAPING 常量精确值。
- 层级矩阵：Stream/DOM/SAX/StAX Result 均 instanceof（StAX 用 XMLOutputFactory writer 构造）。
- 匿名实现 systemId round-trip。
