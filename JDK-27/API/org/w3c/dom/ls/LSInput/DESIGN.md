# org.w3c.dom.ls.LSInput — 设计案

## 1. API 功能
org.w3c.dom.ls.LSInput 接口。实例/声明方法：getCharacterStream, setCharacterStream, getByteStream, setByteStream, getStringData, setStringData, getSystemId, setSystemId, getPublicId, setPublicId, getBaseURI, setBaseURI, getEncoding, setEncoding, getCertifiedText, setCertifiedText。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.ls.LSInput() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.ls.LSInput）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 LSInputTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`LSInputTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 工厂 LSInput 全属性缺省 null/false，7 个属性 setter/getter 往返（stringData/systemId/publicId/baseURI/encoding/certifiedText）。
- 行为闭环：stringData 供 LSParser 消费建树；用户自实现 LSInput（characterStream 通道）同样可解析（duck typing）。
