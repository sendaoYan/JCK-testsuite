# javax.xml.stream.events.Attribute — 设计案

## 1. API 功能
javax.xml.stream.events.Attribute 接口。实例/声明方法：getName, getValue, getDTDType, isSpecified。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.Attribute() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.Attribute）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 AttributeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`AttributeTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 工厂 (local,value)：QName ns=""/prefix=""、DTDType "CDATA"、isSpecified=false（与解析侧 true 相反）、码 10。
- 工厂 (prefix,nsURI,local,value) 参数序 prefix 在前（易写反）；(QName,value) 原样；null local→IAE。
- 解析：文档序 a,b + isSpecified=true + DTDType CDATA + 码 10；带前缀属性 QName 精确；
  reader 从不产出独立 ATTRIBUTE 事件；getAttributeByName 命中/未命中。
- 编码：作为工厂 SE 子项输出 `k='v'`。
