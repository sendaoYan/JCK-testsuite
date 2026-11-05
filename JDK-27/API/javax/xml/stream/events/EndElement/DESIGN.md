# javax.xml.stream.events.EndElement — 设计案

## 1. API 功能
javax.xml.stream.events.EndElement 接口。实例/声明方法：getName, getNamespaces。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.EndElement() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.EndElement）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EndElementTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EndElementTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 解析路径：码 2 + isXxx 族 + asEndElement 自身 + QName 三元组；getNamespaces() 恒空（impl quirk）。
- 工厂三重载：getName 精确；ns 迭代器被吞（getNamespaces 空）。
- 编码 `</p:local>`（与开始元素括号形态不对称）；嵌套 END c→END r→END_DOCUMENT 序。
- asCharacters/asStartElement→CCE；getSchemaType null。
