# javax.xml.stream.Location — 设计案

## 1. API 功能
javax.xml.stream.Location 接口。实例/声明方法：getLineNumber, getColumnNumber, getCharacterOffset, getPublicId, getSystemId。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.Location() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.Location）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 LocationTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`LocationTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
## 5. 契约深化（本轮追加：实现者实测）
- 自定义实现 -1/null 哨兵透传 + toString 覆写。
- 真解析 Location 精确值：多行文档 root SE (1,4)、子元素 line 2 offset 10；Reader 输入双 id null；toString 字段完整。
- 错误定位：未闭合元素异常 Location 指到第 3 行。
