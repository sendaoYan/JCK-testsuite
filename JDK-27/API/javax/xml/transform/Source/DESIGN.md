# javax.xml.transform.Source — 设计案

## 1. API 功能
javax.xml.transform.Source 接口。实例/声明方法：setSystemId, getSystemId, isEmpty。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.Source() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.Source）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SourceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SourceTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- 层级矩阵：Stream/DOM/SAX/StAX Source 均 instanceof。
- systemId 契约（匿名实现 null→set→get→null）。
- JDK-27 版本面：isEmpty() 为默认方法且契约是抛 UnsupportedOperationException("The isEmpty method is not supported.")——不覆写的实现调用即 UOE（探针锁定）；四个内置实现均覆写。
