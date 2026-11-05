# javax.xml.stream.events.EndDocument — 设计案

## 1. API 功能
javax.xml.stream.events.EndDocument 接口。标记接口，无声明成员。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.EndDocument() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.EndDocument）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EndDocumentTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EndDocumentTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 解析：最后事件码 8 + isXxx 族 + getLocation(-1,-1,-1) + getSchemaType null。
- 工厂：码 8；编码输出空串；asCharacters/asStartElement/asEndElement 全 CCE。
- 闭环：工厂事件序列写 writer 后重解析仍以 END_DOCUMENT 收尾。
