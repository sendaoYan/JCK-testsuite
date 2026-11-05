# javax.xml.stream.XMLReporter — 设计案

## 1. API 功能
javax.xml.stream.XMLReporter 接口。实例/声明方法：report。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.XMLReporter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLReporter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLReporterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLReporterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
## 5. 契约深化（本轮追加：安装/触发实测）
- setXMLReporter/getXMLReporter 同一性；默认无 reporter；null 参数容忍。
- 触发实测：重复实体声明/致命错误/正常解析三场景下 **reporter 均未回调**（JDK StAX 无确定触发的非致命告警路径，与 JDK-8 一致）——记录"未触发"并保留结构断言；致命错误以 XMLStreamException 直抛。
